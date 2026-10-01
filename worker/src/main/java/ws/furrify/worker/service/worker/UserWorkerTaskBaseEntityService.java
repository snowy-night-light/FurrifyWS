package ws.furrify.worker.service.worker;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
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
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.worker.domain.worker.UserWorkerTask;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;
import ws.furrify.worker.WorkerApplication;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ws.furrify.core.specification.EntitySpec.specEquals;
import static ws.furrify.core.specification.EntitySpec.specLessThan;
import static ws.furrify.worker.domain.worker.WorkStatus.*;

@Slf4j
public abstract class UserWorkerTaskBaseEntityService<ENTITY extends UserWorkerTask, DTO extends UserWorkerTaskDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> extends BaseEntityCrudService<ENTITY, DTO, PATCH_REQ> {
    protected final AsyncUtils asyncUtils;

    public UserWorkerTaskBaseEntityService(BaseEntityRepository<ENTITY> entityRepository, BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper, AsyncUtils asyncUtils) {
        super(entityRepository, dtoMapper);
        this.asyncUtils = asyncUtils;
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

    @Transactional
    protected void failTask(DTO task, String errorMessage) {
        DTO currentTask = this.internalFindById(task.getId()).orElse(task);
        if (currentTask.getStatus() == CANCELLED) {
            log.info("Task {} is already CANCELLED, not marking as FAILED.", task.getId());
            return;
        }
        log.error("Task {} failed: {}", task.getId(), errorMessage);
        currentTask.setStatus(WorkStatus.FAILED);
        if (currentTask.getErrors() == null) {
            currentTask.setErrors(new ArrayList<>());
        }
        currentTask.getErrors().add(errorMessage != null ? errorMessage : "Unknown error (null message)");
        currentTask.setFinishedAt(ZonedDateTime.now());
        truncateTaskData(currentTask);
        this.internalPutById(currentTask.getId(), currentTask);
    }

    @Transactional
    protected void succeedTask(DTO task) {
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
    }

    @Transactional
    public void triggerExecution(DTO task) {
        if (isShuttingDown) return;
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
    public void deleteById(UUID id) {
        DTO task = getById(id);
        if (task.getStatus() == IN_PROGRESS) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_REMOVAL_WITH_STATUS.getErrorMessage(id, task.getStatus().name()));
        }
        super.deleteById(id);
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

        asyncUtils.runAsyncAfterCommit(() -> {
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
    public DTO create(DTO dto) {
        dto.setLaunchId(WorkerApplication.LAUNCH_ID);
        return super.create(dto);
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
