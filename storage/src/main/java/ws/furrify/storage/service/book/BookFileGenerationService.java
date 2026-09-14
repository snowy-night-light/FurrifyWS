package ws.furrify.storage.service.book;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BookFileUserWorkerTaskDTO;
import org.openapitools.model.CreateBookFileUserWorkerTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.worker.api.BookFileUserWorkerTaskV1RestControllerApiClient;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Slf4j
public class BookFileGenerationService {

    private final ScheduledExecutorService taskScheduler = Executors.newSingleThreadScheduledExecutor();
    private final BookFileUserWorkerTaskV1RestControllerApiClient feignClient;
    private final BookEntityService bookEntityService;
    private final AsyncUtils asyncUtils;
    
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public BookFileGenerationService(
            BookFileUserWorkerTaskV1RestControllerApiClient feignClient,
            @Lazy BookEntityService bookEntityService,
            AsyncUtils asyncUtils) {
        this.feignClient = feignClient;
        this.bookEntityService = bookEntityService;
        this.asyncUtils = asyncUtils;
    }

    @Transactional
    public void scheduleGeneration(UUID bookId) {
        asyncUtils.runAsyncAfterCommit(() -> executeScheduleGeneration(bookId));
    }

    @Transactional
    protected void executeScheduleGeneration(UUID bookId) {
        log.debug("Scheduling file generation task for bookId: {}", bookId);
        
        bookEntityService.markNeedsGeneration(bookId, true);

        ScheduledFuture<?> existingTask = scheduledTasks.get(bookId);
        if (existingTask != null) {
            log.debug("Cancelling existing scheduled task for bookId: {}", bookId);
            existingTask.cancel(false);
        }

        ScheduledFuture<?> newTask = taskScheduler.schedule(
            () -> this.triggerWorker(bookId),
            0, TimeUnit.SECONDS
        );
        
        scheduledTasks.put(bookId, newTask);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedGenerations() {
        log.debug("Checking for books that need file generation retries...");
        
        bookEntityService.getAllPaged("needsBookFileGeneration = true", PageRequest.of(0, 1000)).forEach(book -> {
            if (!scheduledTasks.containsKey(book.getId())) {
                log.debug("Found book {} that needs file generation retry. Scheduling...", book.getId());
                scheduleGeneration(book.getId());
            }
        });

        bookEntityService.getAllPaged("activeWorkerTaskId != null", PageRequest.of(0, 1000)).forEach(book -> {
            if (!scheduledTasks.containsKey(book.getId())) {
                SecurityContextUtils.mockFeignClientSecurityContext(book.getOwnerId());
                try {
                    ResponseEntity<BookFileUserWorkerTaskDTO> response = feignClient.bookFileUserWorkerTaskV1RestControllerGetById(book.getActiveWorkerTaskId());
                    org.openapitools.model.BookFileUserWorkerTaskDTO task = response.getBody();
                    
                    if (task == null || org.openapitools.model.WorkStatus.FAILED.equals(task.getStatus())) {
                        log.warn("Found book {} with FAILED or missing worker task {}. Rescheduling...", book.getId(), book.getActiveWorkerTaskId());
                        scheduleGeneration(book.getId());
                    }
                } catch (feign.FeignException e) {
                    if (e.status() == 404) {
                        log.warn("Found book {} with missing worker task {} (404). Rescheduling...", book.getId(), book.getActiveWorkerTaskId());
                        scheduleGeneration(book.getId());
                    } else {
                        log.error("Failed to fetch worker task status for book {}: HTTP {} - {}", book.getId(), e.status(), e.contentUTF8());
                    }
                } catch (Exception e) {
                    log.error("Failed to check worker task status for book {}: {}", book.getId(), e.getMessage());
                } finally {
                    SecurityContextUtils.clearFeignClientSecurityContext();
                }
            }
        });
    }

    private void triggerWorker(UUID bookId) {
        log.debug("Triggering worker execution for bookId: {}", bookId);
        scheduledTasks.remove(bookId);
        
        bookEntityService.findById(bookId).ifPresent(book -> {
            SecurityContextUtils.mockFeignClientSecurityContext(book.getOwnerId());
            try {
                if (book.getActiveWorkerTaskId() != null) {
                    try {
                        var taskResponse = feignClient.bookFileUserWorkerTaskV1RestControllerGetById(book.getActiveWorkerTaskId()).getBody();
                        if (taskResponse != null && org.openapitools.model.WorkStatus.NOT_STARTED.equals(taskResponse.getStatus())) {
                            log.debug("Cancelling previous active worker task {} for bookId: {}", book.getActiveWorkerTaskId(), bookId);
                            feignClient.bookFileUserWorkerTaskV1RestControllerDelete(book.getActiveWorkerTaskId());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to check or cancel old worker task {}: {}", book.getActiveWorkerTaskId(), e.getMessage());
                    }
                }

                try {
                    CreateBookFileUserWorkerTaskRequest request = new CreateBookFileUserWorkerTaskRequest();
                    request.setSourceBookReferenceId(book.getId());
                    request.setStartAt(ZonedDateTime.now());
                    
                    log.debug("Sending CreateBookFileUserWorkerTaskRequest to worker service for bookId: {}", bookId);
                    var response = feignClient.bookFileUserWorkerTaskV1RestControllerSave(request).getBody();
                    if (response != null && response.getId() != null) {
                        log.debug("Successfully created worker task {} for bookId: {}", response.getId(), bookId);
                        bookEntityService.assignWorkerTask(book.getId(), response.getId());
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    while (cause != null && !(cause instanceof feign.FeignException)) {
                        cause = cause.getCause();
                    }
                    if (cause instanceof feign.FeignException feignException) {
                        log.error("Failed to create new worker task for book {}: HTTP {} - {}", book.getId(), feignException.status(), feignException.contentUTF8());
                    } else {
                        log.error("Failed to create new worker task for book {}: {}", book.getId(), e.getMessage());
                    }
                    bookEntityService.markNeedsGeneration(book.getId(), true);
                }
            } finally {
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        });
    }

    @Transactional
    public void updateWorkerTaskInfo(UUID bookId, Map<String, UUID> newFormatReferenceIds, UUID newActiveWorkerTaskId) {
        log.debug("Delegating worker task info update for bookId: {} to BookEntityService", bookId);
        bookEntityService.updateBookFileWorkerTaskInfo(bookId, newFormatReferenceIds, newActiveWorkerTaskId);
    }
}
