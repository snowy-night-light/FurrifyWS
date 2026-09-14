package ws.furrify.worker.service.worker;

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

import java.time.ZonedDateTime;
import java.util.List;
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

    @Transactional
    protected void failTask(DTO task, String errorMessage) {
        task.setStatus(WorkStatus.FAILED);
        if (task.getErrors() == null) {
            task.setErrors(new java.util.ArrayList<>());
        }
        task.getErrors().add(errorMessage);
        task.setFinishedAt(ZonedDateTime.now());
        this.internalPutById(task.getId(), task);
    }

    @Transactional
    protected void succeedTask(DTO task) {
        task.setStatus(COMPLETED);
        if (task.getErrors() == null) {
            task.setErrors(List.of());
        }
        if (task.getWarnings() == null) {
            task.setWarnings(List.of());
        }
        task.setFinishedAt(ZonedDateTime.now());
        this.internalPutById(task.getId(), task);
    }

    @Transactional
    public void triggerExecution(DTO task) {
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
    public void processImportWorkerTasks() {
        EntitySpecResult<ENTITY> spec = EntitySpec.<ENTITY>specBuilder()
                .where("status", specEquals(NOT_STARTED))
                .and()
                .where("startAt", specLessThan(ZonedDateTime.now()))
                .build();

        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<DTO> tasks = this.getAllPaged(spec.specString(), pageable);
        tasks.forEach(this::processTaskInternal);
    }

    private final ConcurrentHashMap<UUID, Thread> runningTasks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void deleteById(UUID id) {
        DTO task = getById(id);
        if (task.getStatus() == IN_PROGRESS) {
            Thread runningThread = runningTasks.get(id);
            if (runningThread != null) {
                runningThread.interrupt();
            }
        }
        super.deleteById(id);
    }

    @Transactional
    protected void processTaskInternal(DTO task) {
        task.setStatus(IN_PROGRESS);
        task.setStartedAt(ZonedDateTime.now());
        DTO updatedTask = this.internalPutById(task.getId(), task);

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
            }
        });
    }

    protected abstract void processTask(DTO task);
}
