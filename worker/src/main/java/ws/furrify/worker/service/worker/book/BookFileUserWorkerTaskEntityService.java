package ws.furrify.worker.service.worker.book;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.openapitools.model.BookDTO;
import org.openapitools.model.PutBookWorkerTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.EntitySpecUtils;
import org.openapitools.model.BookChapterDTO;
import org.openapitools.model.BookChapterVersionDTO;
import org.openapitools.model.Pageable;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient;
    private final BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient;

    @Autowired
    public BookFileUserWorkerTaskEntityService(BaseEntityRepository<BookFileUserWorkerTask> entityRepository, BaseDTOMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> dtoMapper, AsyncUtils asyncUtils, List<BookFileWorkerGenerator> generators, BookV1RestControllerApiClient bookV1RestControllerApiClient, BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient, BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient) {
        super(entityRepository, dtoMapper, asyncUtils);
        this.generators = generators;
        this.bookV1RestControllerApiClient = bookV1RestControllerApiClient;
        this.bookChapterV1RestControllerApiClient = bookChapterV1RestControllerApiClient;
        this.bookChapterVersionV1RestControllerApiClient = bookChapterVersionV1RestControllerApiClient;
    }

    @Value("${furrify.worker.tasks.book.max-concurrent:1}")
    private int maxConcurrentTasks;

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
            UUID bookId = task.getSourceBookReferenceId();

            BookDTO bookDto;
            try {
                bookDto = bookV1RestControllerApiClient.bookV1RestControllerGetById(bookId).getBody();
            } catch (Exception e) {
                failTask(task, "Failed to fetch book: " + e.getMessage());
                return;
            }
            if (bookDto == null) {
                failTask(task, Errors.REFERENCE_NOT_FOUND.getErrorMessage(bookId));
                return;
            }

            List<BookFileWorkerGenerator.ChapterData> chapterDataList;
            try {
                chapterDataList = fetchChapterData(bookId);
            } catch (Exception e) {
                failTask(task, "Failed to fetch chapter data: " + e.getMessage());
                return;
            }

            Map<String, UUID> formatReferenceIds = new HashMap<>();

            for (var generator : generators) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task thread interrupted for task {}", task.getId());
                    return;
                }
                try {
                    UUID formatId = generator.generate(task, bookDto, chapterDataList);

                    formatReferenceIds.put(generator.getExtension(), formatId);
                } catch (RuntimeException e) {
                    Throwable cause = e.getCause();
                    while (cause != null && !(cause instanceof InterruptedException)) {
                        cause = cause.getCause();
                    }
                    if (Thread.currentThread().isInterrupted() || cause != null) {
                        log.warn("Task thread interrupted during generation for task {}", task.getId());
                    } else {
                        log.error("Generation failed for task {}", task.getId(), e);
                        failTask(task, e.getMessage() != null ? e.getMessage() : e.getClass().getName());
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
                Throwable cause = e.getCause();
                while (cause != null && !(cause instanceof feign.FeignException)) {
                    cause = cause.getCause();
                }
                if (cause instanceof feign.FeignException feignException) {
                    log.error("Failed to update storage service with new formats for book {}: HTTP {} - {}", task.getSourceBookReferenceId(), feignException.status(), feignException.contentUTF8(), e);
                    failTask(task, "Failed to contact storage service to update book formats: HTTP " + feignException.status() + " - " + feignException.contentUTF8());
                } else {
                    String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    log.error("Failed to update storage service with new formats for book {}: {}", task.getSourceBookReferenceId(), errorMessage, e);
                    failTask(task, "Failed to contact storage service to update book formats: " + errorMessage);
                }
                return;
            }

            task.setFormatReferenceIds(formatReferenceIds);
            succeedTask(task);
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }

    private List<BookFileWorkerGenerator.ChapterData> fetchChapterData(UUID bookId) {
        Pageable pageable = new Pageable().page(0).size(1000);

        String chapterSpec = "book.id = " + bookId;
        String encodedChapterSpec = EntitySpecUtils.encodeSpecToBase64(chapterSpec);

        var chaptersResponse = bookChapterV1RestControllerApiClient.bookChapterV1RestControllerGetAllPaged(pageable, encodedChapterSpec).getBody();

        List<BookChapterDTO> chapters;
        if (chaptersResponse != null && chaptersResponse.getContent() != null) {
            chapters = chaptersResponse.getContent();
        } else {
            chapters = Collections.emptyList();
        }

        List<BookFileWorkerGenerator.ChapterData> chapterDataList = new ArrayList<>();
        for (BookChapterDTO chapter : chapters) {
            String versionSpec = "chapter.id = " + chapter.getId();
            String encodedVersionSpec = EntitySpecUtils.encodeSpecToBase64(versionSpec);

            var versionsResponse = bookChapterVersionV1RestControllerApiClient.bookChapterVersionV1RestControllerGetAllPaged(pageable, encodedVersionSpec).getBody();

            List<BookChapterVersionDTO> versions;
            if (versionsResponse != null && versionsResponse.getContent() != null) {
                versions = versionsResponse.getContent();
            } else {
                versions = Collections.emptyList();
            }

            if (versions.isEmpty()) {
                continue;
            }

            BookChapterVersionDTO latestVersion = versions.stream()
                    .max(Comparator.comparing(v -> {
                        if (v.getContentUpdatedAt() != null) {
                            return v.getContentUpdatedAt();
                        } else {
                            return v.getCreatedAt() != null ? v.getCreatedAt() : ZonedDateTime.now().minusYears(100);
                        }
                    }))
                    .orElse(null);

            chapterDataList.add(new BookFileWorkerGenerator.ChapterData(chapter, latestVersion));
        }

        return chapterDataList;
    }

    private static @NonNull PutBookWorkerTaskRequest getPutBookWorkerTaskRequest(Map<String, UUID> formatReferenceIds) {
        PutBookWorkerTaskRequest putRequest = new PutBookWorkerTaskRequest();
        putRequest.setFormatReferenceIds(new HashMap<>(formatReferenceIds));
        putRequest.setActiveWorkerTaskId(null);

        return putRequest;
    }

    @Override
    protected int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    @Override
    protected long getTaskTimeoutSeconds() {
        return 24 * 60 * 60; // 24 hours
    }

    @Override
    protected void onTaskCancelledOnShutdown(BookFileUserWorkerTaskDTO task) {
    }
}
