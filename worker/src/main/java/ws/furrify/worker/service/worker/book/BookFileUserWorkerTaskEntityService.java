package ws.furrify.worker.service.worker.book;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.openapitools.model.PutBookWorkerTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;
import ws.furrify.worker.generator.BookFileWorkerGenerator;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ws.furrify.worker.domain.worker.WorkStatus.IN_PROGRESS;

@Service
@Slf4j
public class BookFileUserWorkerTaskEntityService extends UserWorkerTaskBaseEntityService<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> {

    private final List<BookFileWorkerGenerator> generators;
    private final BookV1RestControllerApiClient bookV1RestControllerApiClient;

    @Autowired
    public BookFileUserWorkerTaskEntityService(BaseEntityRepository<BookFileUserWorkerTask> entityRepository, BaseDTOMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> dtoMapper, AsyncUtils asyncUtils, List<BookFileWorkerGenerator> generators, BookV1RestControllerApiClient bookV1RestControllerApiClient) {
        super(entityRepository, dtoMapper, asyncUtils);
        this.generators = generators;
        this.bookV1RestControllerApiClient = bookV1RestControllerApiClient;
    }


    @Override
    public BookFileUserWorkerTaskDTO create(BookFileUserWorkerTaskDTO dto) {
        try {
            if (bookV1RestControllerApiClient.bookV1RestControllerGetById(dto.getSourceBookReferenceId()).getBody() == null) {
                throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getSourceBookReferenceId()));
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getSourceBookReferenceId()));
        }

        return super.create(dto);
    }

    @Override
    @Transactional
    public BookFileUserWorkerTaskDTO patchById(UUID id, PatchBookFileUserWorkerTaskRequest patchDto) {
        BookFileUserWorkerTaskDTO bookFileUserWorkerTaskDTO = getById(id);
        if (bookFileUserWorkerTaskDTO.getStatus() == IN_PROGRESS) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_UPDATE_WITH_STATUS.getErrorMessage(id, bookFileUserWorkerTaskDTO.getStatus().name()));
        }

        bookFileUserWorkerTaskDTO.setStatus(WorkStatus.NOT_STARTED);

        return super.patchById(id, patchDto);
    }

    @Override
    @Transactional
    protected void processTask(BookFileUserWorkerTaskDTO task) {
        SecurityContextUtils.mockFeignClientSecurityContext(task.getOwnerId());
        try {
            Map<String, UUID> formatReferenceIds = new HashMap<>();

            for (var generator : generators) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task thread interrupted for task {}", task.getId());
                    return;
                }
                try {
                    UUID formatId = generator.generate(task);

                    formatReferenceIds.put(generator.getExtension(), formatId);
                } catch (RuntimeException e) {
                    Throwable cause = e.getCause();
                    while (cause != null && !(cause instanceof InterruptedException)) {
                        cause = cause.getCause();
                    }
                    if (Thread.currentThread().isInterrupted() || cause != null) {
                        log.warn("Task thread interrupted during generation for task {}", task.getId());
                    } else {
                        failTask(task, e.getMessage());
                    }
                    return;
                }
            }

            try {
                PutBookWorkerTaskRequest putRequest = getPutBookWorkerTaskRequest(formatReferenceIds);

                ResponseEntity<Void> response = bookV1RestControllerApiClient.bookV1RestControllerUpdateWorkerTaskInfo(
                    task.getSourceBookReferenceId(),
                    putRequest
                );
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("Failed to update storage service with new formats for book {}: Status code {}", task.getSourceBookReferenceId(), response.getStatusCode());
                    failTask(task, "Failed to contact storage service to update book formats: Status code " + response.getStatusCode());
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to update storage service with new formats for book {}: {}", task.getSourceBookReferenceId(), e.getMessage());
                failTask(task, "Failed to contact storage service to update book formats: " + e.getMessage());
                return;
            }

            task.setFormatReferenceIds(formatReferenceIds);
            succeedTask(task);
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }

    private static @NonNull PutBookWorkerTaskRequest getPutBookWorkerTaskRequest(Map<String, UUID> formatReferenceIds) {
        PutBookWorkerTaskRequest putRequest = new PutBookWorkerTaskRequest();
        putRequest.setFormatReferenceIds(new HashMap<>(formatReferenceIds));
        putRequest.setActiveWorkerTaskId(null);

        return putRequest;
    }
}
