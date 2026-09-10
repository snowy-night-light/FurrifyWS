package ws.furrify.worker.service.worker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import java.util.concurrent.TimeUnit;

import static ws.furrify.core.specification.EntitySpec.specEquals;
import static ws.furrify.core.specification.EntitySpec.specLessThan;
import static ws.furrify.worker.domain.worker.WorkStatus.*;

public abstract class UserWorkerTaskBaseEntityService<ENTITY extends UserWorkerTask, DTO extends UserWorkerTaskDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> extends BaseEntityCrudService<ENTITY, DTO, PATCH_REQ> {
    private final AsyncUtils asyncUtils;

    public UserWorkerTaskBaseEntityService(BaseEntityRepository<ENTITY> entityRepository, BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper, AsyncUtils asyncUtils) {
        super(entityRepository, dtoMapper);
        this.asyncUtils = asyncUtils;
    }

    @Transactional
    protected void failTask(DTO task, String errorMessage) {
        DTO latestTask = this.findById(task.getId()).orElse(task);
        latestTask.setStatus(WorkStatus.FAILED);
        latestTask.setErrors(List.of(errorMessage));
        latestTask.setFinishedAt(ZonedDateTime.now());
        this.internalPutById(latestTask.getId(), latestTask);
    }

    @Transactional
    protected void succeedTask(DTO task) {
        DTO latestTask = this.findById(task.getId()).orElse(task);
        latestTask.setStatus(COMPLETED);
        latestTask.setErrors(List.of());
        latestTask.setWarnings(List.of());
        latestTask.setFinishedAt(ZonedDateTime.now());
        this.internalPutById(latestTask.getId(), latestTask);
    }

    @Transactional
    public void triggerExecution(DTO task) {
        if (task.getStatus() == IN_PROGRESS || task.getStatus() == COMPLETED) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_EXECUTION_WITH_STATUS.getErrorMessage(task.getId(), task.getStatus()));
        }

        task.setStatus(IN_PROGRESS);
        task.setStartedAt(ZonedDateTime.now());
        DTO updatedTask = this.internalPutById(task.getId(), task);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    @Transactional
                    public void afterCommit() {
                        asyncUtils.runAsync(() -> processTaskInternal(updatedTask));
                    }
                }
        );
    }

    @Transactional
    public void triggerExecution(UUID id) {
        DTO task = this.getById(id);

        triggerExecution(task);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    protected void processImportWorkerTasks() {
        EntitySpecResult<ENTITY> spec = EntitySpec.<ENTITY>specBuilder()
                .where("status", specEquals(NOT_STARTED))
                .and()
                .where("startAt", specLessThan(ZonedDateTime.now()))
                .build();

        Pageable pageable = PageRequest.of(0, 30, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<DTO> tasks = this.getAllPaged(spec.specString(), pageable);
        tasks.forEach(this::processTaskInternal);
    }

    @Transactional
    protected void processTaskInternal(DTO task) {
        task.setStatus(IN_PROGRESS);
        task.setStartedAt(ZonedDateTime.now());
        DTO updatedTask = this.internalPutById(task.getId(), task);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    @Transactional
                    public void afterCommit() {
                        asyncUtils.runAsync(() -> processTask(updatedTask));
                    }
                }
        );
    }

    protected abstract void processTask(DTO task);
}
