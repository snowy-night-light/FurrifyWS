/*
 * furrify-worker-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.worker.service.worker;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.service.EurekaDiscoveryService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.worker.WorkerApplication;
import ws.furrify.worker.domain.worker.UserWorkerTask;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ws.furrify.core.specification.EntitySpec.specEquals;
import static ws.furrify.core.specification.EntitySpec.specLessThan;
import static ws.furrify.worker.domain.worker.WorkStatus.*;

@Slf4j
public abstract class UserWorkerTaskBaseEntityService<ENTITY extends UserWorkerTask, DTO extends UserWorkerTaskDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> extends BaseEntityCrudService<ENTITY, DTO, PATCH_REQ> {
    protected final AsyncUtils asyncUtils;

    @Autowired(required = false)
    private EurekaDiscoveryService eurekaDiscoveryService;

    @Value("${storage.service.name:furrify-storage-service}")
    private String storageServiceName;

    @Value("${attachment.service.name:furrify-attachment-service}")
    private String attachmentServiceName;

    public UserWorkerTaskBaseEntityService(BaseEntityRepository<ENTITY> entityRepository, BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper, AsyncUtils asyncUtils) {
        super(entityRepository, dtoMapper);
        this.asyncUtils = asyncUtils;
    }

    public UserWorkerTaskBaseEntityService(BaseEntityRepository<ENTITY> entityRepository, BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper, AsyncUtils asyncUtils, EurekaDiscoveryService eurekaDiscoveryService, String storageServiceName) {
        super(entityRepository, dtoMapper);
        this.asyncUtils = asyncUtils;
        this.eurekaDiscoveryService = eurekaDiscoveryService;
        this.storageServiceName = storageServiceName;
    }

    protected String getStorageServiceName() {
        return (storageServiceName != null && !storageServiceName.isBlank())
                ? storageServiceName
                : "furrify-storage-service";
    }

    protected String getAttachmentServiceName() {
        return (attachmentServiceName != null && !attachmentServiceName.isBlank())
                ? attachmentServiceName
                : "furrify-attachment-service";
    }

    protected boolean isStorageServiceOnline() {
        if (eurekaDiscoveryService == null) {
            log.warn("EurekaDiscoveryService is not available; assuming storage service is online.");
            return true;
        }
        return eurekaDiscoveryService.isServiceOnline(getStorageServiceName());
    }

    protected boolean isAttachmentServiceOnline() {
        if (eurekaDiscoveryService == null) {
            log.warn("EurekaDiscoveryService is not available; assuming attachment service is online.");
            return true;
        }
        return eurekaDiscoveryService.isServiceOnline(getAttachmentServiceName());
    }

    protected boolean areRequiredServicesOnline() {
        return isStorageServiceOnline();
    }

    protected String getRequiredServicesStatusMessage() {
        if (!isStorageServiceOnline()) {
            return "Storage service [" + getStorageServiceName() + "] is not online.";
        }
        return null;
    }

    protected void truncateTaskData(DTO task) {
        final int MAX_LEN = 10000;
        if (task.getErrors() != null) {
            for (int i = 0; i < task.getErrors().size(); i++) {
                String error = task.getErrors().get(i);
                if (error != null && error.length() > MAX_LEN) {
                    task.getErrors().set(i, error.substring(0, MAX_LEN) + "... (truncated)");
                }
            }
        }
        if (task.getWarnings() != null) {
            for (int i = 0; i < task.getWarnings().size(); i++) {
                String warning = task.getWarnings().get(i);
                if (warning != null && warning.length() > MAX_LEN) {
                    task.getWarnings().set(i, warning.substring(0, MAX_LEN) + "... (truncated)");
                }
            }
        }
        if (task.getLog() != null && task.getLog().length() > MAX_LEN) {
            task.setLog(task.getLog().substring(task.getLog().length() - MAX_LEN));
        }
    }

    protected void failTask(DTO task, String errorMessage) {
        failTask(task, errorMessage, null);
    }

    protected void failTask(DTO task, String errorMessage, Throwable t) {
        if (t != null) {
            log.error("Task {} failed: {}", task.getId(), errorMessage, t);
        } else {
            log.error("Task {} failed: {}", task.getId(), errorMessage);
        }
        asyncUtils.runInTransaction(() -> {
            DTO currentTask = this.internalFindById(task.getId()).orElse(task);
            if (currentTask.getStatus() == CANCELLED) {
                log.info("Task {} is already CANCELLED, not marking as FAILED.", task.getId());
                return;
            }
            currentTask.setStatus(WorkStatus.FAILED);
            if (currentTask.getErrors() == null) {
                currentTask.setErrors(new ArrayList<>());
            }
            currentTask.getErrors().add(errorMessage != null ? errorMessage : "Unknown error (null message)");
            currentTask.setFinishedAt(ZonedDateTime.now());
            truncateTaskData(currentTask);
            this.internalPutById(currentTask.getId(), currentTask);
        });
    }

    protected void rescheduleTask(DTO task, String reason) {
        asyncUtils.runInTransaction(() -> {
            DTO currentTask = this.internalFindById(task.getId()).orElse(task);
            if (currentTask.getStatus() == CANCELLED) {
                log.info("Task {} is already CANCELLED, not rescheduling.", task.getId());
                return;
            }
            log.warn("Task {} rescheduled (will retry on next poll): {}", task.getId(), reason);
            currentTask.setStatus(NOT_STARTED);
            currentTask.setStartedAt(null);
            this.internalPutById(currentTask.getId(), currentTask);
        });
    }

    protected void succeedTask(DTO task) {
        asyncUtils.runInTransaction(() -> {
            DTO currentTask = this.internalFindById(task.getId()).orElse(task);
            if (currentTask.getStatus() == CANCELLED) {
                log.info("Task {} is already CANCELLED, not marking as COMPLETED.", task.getId());
                return;
            }
            currentTask.setStatus(COMPLETED);
            if (currentTask.getErrors() == null) {
                currentTask.setErrors(List.of());
            }
            if (currentTask.getWarnings() == null) {
                currentTask.setWarnings(List.of());
            }
            currentTask.setFinishedAt(ZonedDateTime.now());
            truncateTaskData(currentTask);
            this.internalPutById(currentTask.getId(), currentTask);
        });
    }

    @Transactional
    public void triggerExecution(DTO task) {
        if (isShuttingDown) return;
        if (!areRequiredServicesOnline()) {
            String statusMsg = getRequiredServicesStatusMessage();
            log.warn("Cannot trigger execution of task {}: {}", task.getId(), statusMsg);
            throw new ServiceLogicException(statusMsg != null ? statusMsg : "Required services are not online.");
        }
        if (task.getStatus() == IN_PROGRESS || task.getStatus() == COMPLETED) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_EXECUTION_WITH_STATUS.getErrorMessage(task.getId(), task.getStatus().name()));
        }

        processTaskInternal(task);
    }

    @Transactional
    public void triggerExecution(UUID id) {
        DTO task = this.getById(id);
        triggerExecution(task);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void processWorkerTasks() {
        if (isShuttingDown) return;

        if (!areRequiredServicesOnline()) {
            log.warn("Skipping fetching of tasks for {}: {}", getClass().getSimpleName(), getRequiredServicesStatusMessage());
            return;
        }

        int currentlyRunning = runningTasks.size();
        int maxConcurrent = getMaxConcurrentTasks();
        log.info("Checking for worker tasks... currentlyRunning: {}, maxConcurrent: {}", currentlyRunning, maxConcurrent);

        if (currentlyRunning >= maxConcurrent) {
            log.warn("Worker queue is full! Currently running: {}, Max: {}", currentlyRunning, maxConcurrent);
            return;
        }

        int toFetch = maxConcurrent - currentlyRunning;

        EntitySpecResult<ENTITY> spec = EntitySpec.<ENTITY>specBuilder()
                .where("status", specEquals(NOT_STARTED))
                .and()
                .where("startAt", specLessThan(ZonedDateTime.now()))
                .build();

        Pageable pageable = PageRequest.of(0, toFetch, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<DTO> tasks = this.getAllPaged(spec.specString(), pageable);
        log.info("Found {} NOT_STARTED tasks to process", tasks.getTotalElements());
        tasks.forEach(this::processTaskInternal);
    }

    protected int getMaxConcurrentTasks() {
        return 5;
    }

    private final ConcurrentHashMap<UUID, Object> runningTasks = new ConcurrentHashMap<>();
    private volatile boolean isShuttingDown = false;

    @Override
    @Transactional
    protected Optional<DTO> handleDelete(UUID id) {
        DTO task = getById(id);
        if (task.getStatus() == IN_PROGRESS) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_REMOVAL_WITH_STATUS.getErrorMessage(id, task.getStatus().name()));
        }
        return super.handleDelete(id);
    }

    @Transactional
    public void cancelById(UUID id) {
        DTO task = getById(id);
        if (task.getStatus() == IN_PROGRESS) {
            Object runningValue = runningTasks.get(id);
            if (runningValue instanceof Thread) {
                ((Thread) runningValue).interrupt();
            }
        }
        
        task.setStatus(WorkStatus.CANCELLED);
        this.internalPutById(id, task);
    }

    @Transactional
    protected void processTaskInternal(DTO task) {
        if (!areRequiredServicesOnline()) {
            log.warn("Skipping execution of task {}: {}", task.getId(), getRequiredServicesStatusMessage());
            return;
        }

        DTO lockedTask = this.internalFindById(task.getId()).orElse(null);
        if (lockedTask == null) {
            log.debug("Task {} was deleted before it could be processed. Skipping.", task.getId());
            return;
        }
        if (lockedTask.getStatus() != WorkStatus.NOT_STARTED) {
            log.debug("Task {} was already started by another worker. Skipping.", task.getId());
            return;
        }
        
        lockedTask.setStatus(IN_PROGRESS);
        lockedTask.setStartedAt(ZonedDateTime.now());
        lockedTask.setLaunchId(WorkerApplication.LAUNCH_ID);
        DTO updatedTask = this.internalPutById(lockedTask.getId(), lockedTask);

        // Pre-register as running
        runningTasks.put(updatedTask.getId(), Boolean.TRUE);

        asyncUtils.runAsyncAfterCommitNoTransaction(() -> {
            runningTasks.put(updatedTask.getId(), Thread.currentThread());
            try {
                processTask(updatedTask);
            } catch (Throwable e) {
                log.error("FATAL UNCAUGHT ERROR in processTask for task {}: ", updatedTask.getId(), e);
                try {
                    failTask(updatedTask, "Fatal error: " + e.getMessage());
                } catch (Throwable inner) {
                    log.error("Failed to even call failTask for task {}: ", updatedTask.getId(), inner);
                }
            } finally {
                runningTasks.remove(updatedTask.getId());
                // Immediately check for and start the next task to prevent delay
                asyncUtils.runInTransaction(this::processWorkerTasks);
            }
        });
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cancelStuckTasks() {
        ZonedDateTime timeoutThreshold = ZonedDateTime.now().minusSeconds(getTaskTimeoutSeconds());

        EntitySpecResult<ENTITY> spec = EntitySpec.<ENTITY>specBuilder()
                .where("status", specEquals(IN_PROGRESS))
                .and()
                .where("startedAt", specLessThan(timeoutThreshold))
                .build();

        Pageable pageable = PageRequest.of(0, 100);
        Page<DTO> tasks = this.getAllPaged(spec.specString(), pageable);

        tasks.forEach(task -> {
            log.warn("Task {} has been IN_PROGRESS since {}. It exceeded the timeout of {} seconds. Marking as FAILED.", task.getId(), task.getStartedAt(), getTaskTimeoutSeconds());
            failTask(task, "Task timed out after exceeding the maximum allowed execution time.");

            Object runningValue = runningTasks.get(task.getId());
            if (runningValue instanceof Thread) {
                ((Thread) runningValue).interrupt();
            }
        });
    }

    protected abstract long getTaskTimeoutSeconds();

    protected abstract void processTask(DTO task);

    @Override
    protected DTO handleCreate(DTO dto) {
        dto.setLaunchId(WorkerApplication.LAUNCH_ID);
        return super.handleCreate(dto);
    }

    @PreDestroy
    @Transactional
    public void cancelRunningTasksOnShutdown() {
        isShuttingDown = true;
        asyncUtils.runInTransaction(() -> {
            log.info("Executing graceful shutdown: cancelling tasks with launchId {}", WorkerApplication.LAUNCH_ID);
            EntitySpecResult<ENTITY> spec = EntitySpec.<ENTITY>specBuilder()
                    .where("launchId", specEquals(WorkerApplication.LAUNCH_ID))
                    .and()
                    .where("status", specEquals(IN_PROGRESS))
                    .build();

            List<DTO> tasks = this.getAllInternal(spec.specString());
            log.info("Found {} running tasks to cancel.", tasks.size());
            
            tasks.forEach(task -> {
                log.info("Cancelling task {}", task.getId());
                task.setStatus(CANCELLED);
                if (task.getErrors() == null) {
                    task.setErrors(new ArrayList<>());
                }
                task.getErrors().add("Task was cancelled due to service shutdown.");
                task.setFinishedAt(ZonedDateTime.now());
                
                onTaskCancelledOnShutdown(task);
                
                this.internalPutById(task.getId(), task);

                Object runningValue = runningTasks.get(task.getId());
                if (runningValue instanceof Thread) {
                    ((Thread) runningValue).interrupt();
                }
            });
        });
    }

    protected abstract void onTaskCancelledOnShutdown(DTO task);
}
