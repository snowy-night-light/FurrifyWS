/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.service.book;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BookFileUserWorkerTaskDTO;
import org.openapitools.model.CreateBookFileUserWorkerTaskRequest;
import org.openapitools.model.Pageable;
import org.openapitools.model.WorkStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.worker.api.BookFileUserWorkerTaskV1RestControllerApiClient;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookFileGenerationService {

    private final ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
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

    public void executeScheduleGeneration(UUID bookId) {
        log.debug("Scheduling file generation task for bookId: {}", bookId);
        
        asyncUtils.runInTransaction(() -> bookEntityService.markNeedsGeneration(bookId, true));

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
    public void retryFailedGenerations() {
        log.debug("Checking for books that need file generation retries...");

        bookEntityService.getAllPaged("needsBookFileGeneration = true", PageRequest.of(0, 1000)).forEach(book -> {
            if (!scheduledTasks.containsKey(book.getId())) {
                log.debug("Found book {} that needs file generation retry. Scheduling...", book.getId());
                this.executeScheduleGeneration(book.getId());
            }
        });

        Map<UUID, List<BookDTO>> groupedBooks = bookEntityService.getAllPaged("activeWorkerTaskId != null", PageRequest.of(0, 1000))
                .stream()
                .filter(book -> !scheduledTasks.containsKey(book.getId()))
                .collect(Collectors.groupingBy(BookDTO::getOwnerId));

        groupedBooks.forEach((ownerId, books) -> {
            String spec = books.stream()
                    .map(book -> "id = " + book.getActiveWorkerTaskId())
                    .collect(Collectors.joining(" or "));

            SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
            try {
                Pageable pageable = new Pageable()
                        .page(0)
                        .size(1000);

                var response = feignClient.bookFileUserWorkerTaskV1RestControllerGetAllPaged(pageable, spec);
                var pageBody = response.getBody();

                Map<UUID, BookFileUserWorkerTaskDTO> taskMap = (pageBody != null && pageBody.getContent() != null ? pageBody.getContent() : java.util.Collections.<BookFileUserWorkerTaskDTO>emptyList())
                        .stream()
                        .collect(Collectors.toMap(BookFileUserWorkerTaskDTO::getId, task -> task));

                books.forEach(book -> {
                    BookFileUserWorkerTaskDTO task = taskMap.get(book.getActiveWorkerTaskId());

                    if (task == null || WorkStatus.FAILED.equals(task.getStatus())) {
                        log.warn("Found book {} with FAILED or missing worker task {}. Rescheduling...", book.getId(), book.getActiveWorkerTaskId());
                        executeScheduleGeneration(book.getId());
                    }
                });
            } catch (FeignException e) {
                log.error("Failed to fetch worker task status batch for owner {}: HTTP {} - {}", ownerId, e.status(), e.contentUTF8());
            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker is OPEN. Cannot check worker task status batch for owner {}. Will retry later.", ownerId);
            } catch (Exception e) {
                log.error("Failed to check worker task status batch for owner {}: {}", ownerId, e.getMessage());
            } finally {
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        });
        }

    private void triggerWorker(UUID bookId) {
        log.debug("Triggering worker execution for bookId: {}", bookId);
        scheduledTasks.remove(bookId);
        
        BookDTO book = bookEntityService.claimBookForGeneration(bookId);
        if (book == null) {
            log.debug("Book {} was already claimed for generation or does not need it. Skipping.", bookId);
            return;
        }

        SecurityContextUtils.mockFeignClientSecurityContext(book.getOwnerId());
        try {
            if (book.getActiveWorkerTaskId() != null) {
                try {
                    var taskResponse = feignClient.bookFileUserWorkerTaskV1RestControllerGetById(book.getActiveWorkerTaskId()).getBody();
                    if (taskResponse != null) {
                        if (WorkStatus.NOT_STARTED.equals(taskResponse.getStatus())) {
                            log.debug("Cancelling previous active worker task {} for bookId: {}", book.getActiveWorkerTaskId(), bookId);
                            feignClient.bookFileUserWorkerTaskV1RestControllerCancel(book.getActiveWorkerTaskId());
                        } else if (WorkStatus.IN_PROGRESS.equals(taskResponse.getStatus())) {
                            log.debug("Previous worker task {} for bookId: {} is still IN_PROGRESS. Leaving it to finish and reverting claim.", book.getActiveWorkerTaskId(), bookId);
                            bookEntityService.markNeedsGeneration(book.getId(), true);
                            return;
                        }
                    }
                } catch (CallNotPermittedException e) {
                    log.warn("Circuit breaker is OPEN. Cannot check or cancel old worker task {}. Will retry later.", book.getActiveWorkerTaskId());
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
                while (cause != null && !(cause instanceof FeignException)) {
                    cause = cause.getCause();
                }
                if (cause instanceof FeignException feignException) {
                    log.error("Failed to create new worker task for book {}: HTTP {} - {}", book.getId(), feignException.status(), feignException.contentUTF8());
                } else if (e instanceof CallNotPermittedException) {
                    log.warn("Circuit breaker is OPEN. Cannot create new worker task for book {}. Will retry later.", book.getId());
                } else {
                    log.error("Failed to create new worker task for book {}: {}", book.getId(), e.getMessage());
                }
                bookEntityService.markNeedsGeneration(book.getId(), true);
            }
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }

    @Transactional
    public void updateWorkerTaskInfo(UUID bookId, Map<String, UUID> newFormatReferenceIds, UUID newActiveWorkerTaskId) {
        log.debug("Delegating worker task info update for bookId: {} to BookEntityService", bookId);
        bookEntityService.updateBookFileWorkerTaskInfo(bookId, newFormatReferenceIds, newActiveWorkerTaskId);
    }
}
