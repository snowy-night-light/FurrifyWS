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
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.EntitySpecUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.worker.api.BookFileUserWorkerTaskV1RestControllerApiClient;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookFileGenerationService {

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

    /**
     * Books removed without loading them (library removal): drop their pending generations here and cancel
     * their worker tasks, so the worker doesn't build files for books that are gone. Best effort: a task
     * that can't be cancelled fails on its own once it finds no book.
     *
     * @param tasksByOwner the removed books' active worker tasks, by the books' owner
     */
    public void forgetRemovedBooks(java.util.Collection<UUID> bookIds, Map<UUID, List<UUID>> tasksByOwner) {
        for (UUID bookId : bookIds) {
            ScheduledFuture<?> pending = scheduledTasks.remove(bookId);
            if (pending != null) pending.cancel(false);
        }
        tasksByOwner.forEach((ownerId, taskIds) -> {
            if (taskIds.isEmpty()) return;
            SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
            try {
                feignClient.bookFileUserWorkerTaskV1RestControllerCancelBulk(taskIds);
            } catch (Exception e) {
                log.warn("Could not cancel {} worker tasks of removed books: {}", taskIds.size(), unwrapCause(e).getMessage());
            } finally {
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        });
    }

    public void executeScheduleGeneration(UUID bookId) {
        log.debug("Scheduling file generation task for bookId: {}", bookId);
        
        bookEntityService.markNeedsGeneration(bookId, true);

        ScheduledFuture<?> existingTask = scheduledTasks.get(bookId);
        if (existingTask != null) {
            log.debug("Cancelling existing scheduled task for bookId: {}", bookId);
            existingTask.cancel(false);
        }

        ScheduledFuture<?> newTask = asyncUtils.runAsyncLaterNoTransaction(() -> this.triggerWorker(bookId), Duration.ofSeconds(30));
        
        scheduledTasks.put(bookId, newTask);
    }

    @Scheduled(fixedDelay = 60000)
    public void retryFailedGenerations() {
        log.debug("Checking for books that need file generation retries...");

        bookEntityService.findBookIdsNeedingGeneration(1000).forEach(bookId -> {
            if (!scheduledTasks.containsKey(bookId)) {
                log.debug("Found book {} that needs file generation retry. Scheduling...", bookId);
                this.executeScheduleGeneration(bookId);
            }
        });

        Map<UUID, List<BookRepository.BookActiveTaskProjection>> groupedBooks = bookEntityService.findBooksWithActiveWorkerTask(1000)
                .stream()
                .filter(book -> !scheduledTasks.containsKey(book.getId()))
                .collect(Collectors.groupingBy(BookRepository.BookActiveTaskProjection::getOwnerId));

        boolean[] circuitBreakerOpen = {false};

        for (Map.Entry<UUID, List<BookRepository.BookActiveTaskProjection>> entry : groupedBooks.entrySet()) {
            if (circuitBreakerOpen[0]) {
                break;
            }
            UUID ownerId = entry.getKey();
            List<BookRepository.BookActiveTaskProjection> books = entry.getValue();

            int batchSize = 100;
            for (int i = 0; i < books.size(); i += batchSize) {
                if (circuitBreakerOpen[0]) {
                    break;
                }
                List<BookRepository.BookActiveTaskProjection> batch = books.subList(i, Math.min(i + batchSize, books.size()));

                List<String> taskIds = batch.stream()
                        .map(BookRepository.BookActiveTaskProjection::getActiveWorkerTaskId)
                        .filter(Objects::nonNull)
                        .map(UUID::toString)
                        .distinct()
                        .toList();

                if (taskIds.isEmpty()) {
                    continue;
                }

                String spec = taskIds.stream()
                        .map(id -> "id = " + id)
                        .collect(Collectors.joining(" || "));

                String encodedSpec = EntitySpecUtils.encodeSpecToBase64(spec);

                SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
                try {
                    Pageable pageable = new Pageable()
                            .page(0)
                            .size(batchSize);

                    var response = feignClient.bookFileUserWorkerTaskV1RestControllerGetAllPaged(pageable, encodedSpec);
                    var pageBody = response.getBody();

                    Map<UUID, BookFileUserWorkerTaskDTO> taskMap = (pageBody != null && pageBody.getContent() != null ? pageBody.getContent() : java.util.Collections.<BookFileUserWorkerTaskDTO>emptyList())
                            .stream()
                            .collect(Collectors.toMap(BookFileUserWorkerTaskDTO::getId, task -> task, (existing, replacement) -> existing));

                    batch.forEach(book -> {
                        BookFileUserWorkerTaskDTO task = taskMap.get(book.getActiveWorkerTaskId());

                        if (task == null || WorkStatus.FAILED.equals(task.getStatus()) || WorkStatus.CANCELLED.equals(task.getStatus())) {
                            int retries = book.getGenerationRetryCount() != null ? book.getGenerationRetryCount() : 0;
                            if (retries >= 3) {
                                log.error("Book {} exceeded max file generation retries ({}/3). Marking generation as failed.", book.getId(), retries);
                                bookEntityService.markFileGenerationFailed(book.getId());
                            } else {
                                log.warn("Found book {} with FAILED/CANCELLED or missing worker task {} (attempt {}/3). Rescheduling...", book.getId(), book.getActiveWorkerTaskId(), retries + 1);
                                bookEntityService.incrementGenerationRetryCount(book.getId());
                                bookEntityService.clearActiveWorkerTaskId(book.getId());
                                executeScheduleGeneration(book.getId());
                            }
                        } else if (WorkStatus.COMPLETED.equals(task.getStatus())) {
                            log.debug("Found book {} with COMPLETED worker task {} still active. Clearing active task ID...", book.getId(), book.getActiveWorkerTaskId());
                            bookEntityService.clearActiveWorkerTaskId(book.getId());
                            bookEntityService.resetGenerationRetryCount(book.getId());
                        }
                    });
                } catch (Exception e) {
                    if (isCircuitBreakerOpen(e)) {
                        circuitBreakerOpen[0] = true;
                        log.warn("Circuit breaker is OPEN. Cannot check worker task status batch for owner {}. Stopping status checks for this cycle.", ownerId);
                    } else {
                        Throwable cause = unwrapCause(e);
                        if (cause instanceof FeignException feignException) {
                            log.error("Failed to fetch worker task status batch for owner {}: HTTP {} - {}", ownerId, feignException.status(), feignException.contentUTF8());
                        } else {
                            log.error("Failed to check worker task status batch for owner {}: {}", ownerId, cause.getMessage());
                        }
                    }
                } finally {
                    SecurityContextUtils.clearFeignClientSecurityContext();
                }
            }
        }
    }

    private void triggerWorker(UUID bookId) {
        log.debug("Triggering worker execution for bookId: {}", bookId);
        scheduledTasks.computeIfPresent(bookId, (k, task) -> task.isDone() ? null : task);
        
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
                } catch (Exception e) {
                    if (isCircuitBreakerOpen(e)) {
                        log.warn("Circuit breaker is OPEN. Cannot check or cancel old worker task {}. Will retry later.", book.getActiveWorkerTaskId());
                    } else {
                        Throwable cause = unwrapCause(e);
                        if (cause instanceof FeignException feignException) {
                            log.warn("Failed to check or cancel old worker task {}: HTTP {} - {}", book.getActiveWorkerTaskId(), feignException.status(), feignException.contentUTF8());
                        } else {
                            log.warn("Failed to check or cancel old worker task {}: {}", book.getActiveWorkerTaskId(), cause.getMessage());
                        }
                    }
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
                if (isCircuitBreakerOpen(e)) {
                    log.warn("Circuit breaker is OPEN. Cannot create new worker task for book {}. Will retry later.", book.getId());
                } else {
                    Throwable cause = unwrapCause(e);
                    if (cause instanceof FeignException feignException) {
                        log.error("Failed to create new worker task for book {}: HTTP {} - {}", book.getId(), feignException.status(), feignException.contentUTF8());
                    } else {
                        log.error("Failed to create new worker task for book {}: {}", book.getId(), cause.getMessage());
                    }
                }
                bookEntityService.markNeedsGeneration(book.getId(), true);
            }
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }

    private static boolean isCircuitBreakerOpen(Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof CallNotPermittedException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static Throwable unwrapCause(Throwable t) {
        Throwable cause = t;
        while (cause instanceof NoFallbackAvailableException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause != null ? cause : t;
    }

    @Transactional
    public void updateWorkerTaskInfo(UUID bookId, Map<String, UUID> newFormatReferenceIds, UUID newActiveWorkerTaskId) {
        log.debug("Delegating worker task info update for bookId: {} to BookEntityService", bookId);
        bookEntityService.updateBookFileWorkerTaskInfo(bookId, newFormatReferenceIds, newActiveWorkerTaskId);
    }
}
