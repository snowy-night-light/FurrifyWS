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

import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.book.chapter.BookChapterRepository;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.service.artist.ArtistEntityService;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.service.tag.TagEntityService;
import ws.furrify.storage.shared.exception.StorageErrors;
import ws.furrify.storage.shared.util.ContentHtmlSanitizerUtil;

import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class BookEntityService extends BaseEntityCrudService<Book, BookDTO, PatchBookRequest> {

    private final MediaEntityService mediaEntityService;
    private final BookChapterRepository bookChapterRepository;
    private final BookRepository bookRepository;
    private final TagEntityService tagEntityService;
    private final ArtistEntityService artistEntityService;
    private final LibraryEntityService libraryEntityService;
    private final SourceEntityService sourceEntityService;
    private final BookFileGenerationService bookFileGenerationService;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final AsyncUtils asyncUtils;

    private final Map<UUID, ScheduledFuture<?>> pendingWordCountTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> firstScheduledTimes = new ConcurrentHashMap<>();

    @Autowired
    public BookEntityService(BookRepository bookRepository, BaseDTOMapper<Book, BookDTO, PatchBookRequest> dtoMapper, MediaEntityService mediaEntityService, BookChapterRepository bookChapterRepository, TagEntityService tagEntityService, ArtistEntityService artistEntityService, LibraryEntityService libraryEntityService, SourceEntityService sourceEntityService, @Lazy BookFileGenerationService bookFileGenerationService, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, AsyncUtils asyncUtils) {
        super(bookRepository, dtoMapper);
        this.bookRepository = bookRepository;
        this.bookChapterRepository = bookChapterRepository;
        this.mediaEntityService = mediaEntityService;
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.libraryEntityService = libraryEntityService;
        this.sourceEntityService = sourceEntityService;
        this.bookFileGenerationService = bookFileGenerationService;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.asyncUtils = asyncUtils;
    }

    @Override
    @Transactional
    protected BookDTO handleCreate(BookDTO dto) {
        this.handleInternalReference(dto, BookDTO::getCover, BookDTO::setCover, mediaEntityService);
        this.handleInternalReference(dto, BookDTO::getLibrary, BookDTO::setLibrary, libraryEntityService);
        if (dto.getSequels() != null) {
            dto.getSequels().forEach(seqId -> {
                if (!this.existsById(seqId)) throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(seqId));
            });
        }
        if (dto.getPrequels() != null) {
            dto.getPrequels().forEach(preId -> {
                if (!this.existsById(preId)) throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(preId));
            });
        }
        this.handleInternalCollectionReferences(dto, BookDTO::getTags, BookDTO::setTags, tagEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getArtists, BookDTO::setArtists, artistEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getSources, BookDTO::setSources, sourceEntityService);

        // Later calculated
        dto.setTotalWordCount(0L);

        // Sanitize html
        dto.setDescriptionHtml(sanitizeHtml(dto.getDescriptionHtml()));
        dto.setShortDescriptionHtml(sanitizeHtml(dto.getShortDescriptionHtml()));

        dto.setNeedsBookFileGeneration(true);

        // Verify user can update likes and dislikes based on library setting
        LibraryDTO libraryDTO = dto.getLibrary();
        checkLikesEnabled(libraryDTO, dto.getLikes(), dto.getDislikes());

        return super.handleCreate(dto);
    }

    @Override
    @Transactional
    protected BookDTO handlePatch(UUID id, PatchBookRequest patchDto) {
        this.handleInternalReference(patchDto.getCover(), mediaEntityService);
        if (patchDto.getSequels() != null && patchDto.getSequels().isPresent() && patchDto.getSequels().get() != null) {
            patchDto.getSequels().get().forEach(req -> {
                if (req != null && !this.existsById(req.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(req.getId()));
                }
            });
        }
        if (patchDto.getPrequels() != null && patchDto.getPrequels().isPresent() && patchDto.getPrequels().get() != null) {
            patchDto.getPrequels().get().forEach(req -> {
                if (req != null && !this.existsById(req.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(req.getId()));
                }
            });
        }
        this.handleInternalReference(patchDto.getLibrary(), libraryEntityService);
        this.handleCollectionInternalReferences(patchDto.getTags(), tagEntityService);
        this.handleCollectionInternalReferences(patchDto.getArtists(), artistEntityService);
        this.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        // Sanitize html
        if (patchDto.getDescriptionHtml().isPresent()) {
            patchDto.setDescriptionHtml(JsonNullable.of(sanitizeHtml(patchDto.getDescriptionHtml().get())));
        }
        if (patchDto.getShortDescriptionHtml().isPresent()) {
            patchDto.setShortDescriptionHtml(JsonNullable.of(sanitizeHtml(patchDto.getShortDescriptionHtml().get())));
        }

        // Verify user can update likes and dislikes based on library setting (only load the book
        // when the patch actually touches them; checkLikesEnabled is a no-op otherwise)
        if (patchDto.getLikes().isPresent() || patchDto.getDislikes().isPresent()) {
            LibraryDTO libraryDTO = super.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id))).getLibrary();
            checkLikesEnabled(libraryDTO, patchDto.getLikes().orElse(null), patchDto.getDislikes().orElse(null));
        }

        boolean needsRegeneration = patchDto.getTitle().isPresent() ||
                patchDto.getDescriptionHtml().isPresent() ||
                patchDto.getShortDescriptionHtml().isPresent() ||
                patchDto.getArtists().isPresent() ||
                patchDto.getTags().isPresent() ||
                patchDto.getCover().isPresent();

        BookDTO patchedBook = super.handlePatch(id, patchDto);

        if (needsRegeneration) {
            asyncUtils.runAsyncAfterCommitNoTransaction(() -> bookFileGenerationService.executeScheduleGeneration(id));
        }

        return patchedBook;
    }

    @Override
    @Transactional
    protected Optional<BookDTO> handleDelete(UUID id) {
        // Book files are deleted after commit by BookEntityListener (also on library deletion).
        // The cover is not deleted here: unused media are removed by MediaOrphanCleanupService.
        return super.handleDelete(id);
    }

    private String sanitizeHtml(String html) {
        return ContentHtmlSanitizerUtil.sanitize(html);
    }

    private void checkLikesEnabled(LibraryDTO libraryDTO, Integer likes, Integer dislikes) {
        if (dislikes != null && !libraryDTO.getDislikesEnabled()) {
            throw new ServiceLogicException(StorageErrors.DISLIKES_DISABLED_EXCEPTION.getErrorMessage(libraryDTO.getId()));
        }

        if (likes != null && !libraryDTO.getLikesEnabled()) {
            throw new ServiceLogicException(StorageErrors.LIKES_DISABLED_EXCEPTION.getErrorMessage(libraryDTO.getId()));
        }
    }

    @Transactional
    public void updateBookTotalWordCount(UUID bookId) {
        if (bookId == null) {
            return;
        }

        long bookWordCount = bookChapterRepository.sumWordsByBookId(bookId);
        bookRepository.updateTotalWordCountAndMarkNeedsGeneration(bookId, bookWordCount);
        bookRepository.refreshChaptersCount(bookId);

        asyncUtils.runAsyncAfterCommitNoTransaction(() -> this.bookFileGenerationService.executeScheduleGeneration(bookId));
    }

    public void updateBookTotalWordCountAsync(UUID bookId) {
        if (bookId == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long firstTime = firstScheduledTimes.putIfAbsent(bookId, now);
        if (firstTime == null) {
            firstTime = now;
        }

        ScheduledFuture<?> existing = pendingWordCountTasks.get(bookId);
        if (existing != null && (now - firstTime < 5000)) {
            existing.cancel(false);
        } else if (existing != null && !existing.isDone()) {
            return;
        }

        ScheduledFuture<?> newTask = asyncUtils.runAsyncLaterNoTransaction(() -> {
            try {
                pendingWordCountTasks.remove(bookId);
                firstScheduledTimes.remove(bookId);
                asyncUtils.runInTransaction(() -> this.updateBookTotalWordCount(bookId));
            } catch (Exception e) {
                log.error("Failed to execute debounced total word count update for bookId: {}", bookId, e);
            }
        }, java.time.Duration.ofSeconds(1));

        pendingWordCountTasks.put(bookId, newTask);
    }

    /** Up to {@code limit} books waiting for their files to be generated (not given up on). */
    @Transactional(readOnly = true)
    public List<UUID> findBookIdsNeedingGeneration(int limit) {
        return bookRepository.findBookIdsNeedingGeneration(PageRequest.of(0, limit));
    }

    /** Up to {@code limit} books with a worker task generating their files. */
    @Transactional(readOnly = true)
    public List<BookRepository.BookActiveTaskProjection> findBooksWithActiveWorkerTask(int limit) {
        return bookRepository.findBooksWithActiveWorkerTask(PageRequest.of(0, limit));
    }

    @Transactional
    public void markNeedsGeneration(UUID bookId, boolean needsGeneration) {
        if (bookId != null) {
            bookRepository.updateNeedsBookFileGeneration(bookId, needsGeneration);
        }
    }

    @Transactional
    public BookDTO claimBookForGeneration(UUID bookId) {
        if (bookId == null) {
            return null;
        }
        int updated = bookRepository.claimBookForGeneration(bookId);
        if (updated > 0) {
            return this.findById(bookId).orElse(null);
        }
        return null;
    }

    @Transactional
    public void updateBookFileWorkerTaskInfo(UUID bookId, Map<String, UUID> newFormatReferenceIds, UUID newActiveWorkerTaskId) {
        BookDTO book = this.internalFindById(bookId).orElse(null);
        if (book != null) {
            if (newFormatReferenceIds != null) {
                if (book.getFormatReferenceIds() != null) {
                    // replaced (or dropped) files are deleted once this is committed
                    Set<UUID> oldAttachments = new HashSet<>(book.getFormatReferenceIds().values());
                    oldAttachments.removeAll(new HashSet<>(newFormatReferenceIds.values()));
                    oldAttachments.remove(null);
                    deleteAttachmentFilesAsync(bookId, oldAttachments);
                    book.getFormatReferenceIds().clear();
                    book.getFormatReferenceIds().putAll(newFormatReferenceIds);
                } else {
                    book.setFormatReferenceIds(newFormatReferenceIds);
                }
            }
            book.setActiveWorkerTaskId(newActiveWorkerTaskId);
            book.setGenerationRetryCount(0);
            book.setFileGenerationFailed(false);
            this.internalPutById(bookId, book);
        }
    }

    @Transactional
    public void assignWorkerTask(UUID bookId, UUID workerTaskId) {
        BookDTO book = this.internalFindById(bookId).orElse(null);
        if (book != null) {
            book.setActiveWorkerTaskId(workerTaskId);
            // The current files stay until the worker delivers the new ones: updateBookFileWorkerTaskInfo
            // then deletes the ones that were replaced. Readers keep a file meanwhile, and a failed
            // generation leaves the book with its old files instead of none.
            book.setNeedsBookFileGeneration(false);
            this.internalPutById(bookId, book);
        }
    }

    @Transactional
    public void clearActiveWorkerTaskId(UUID bookId) {
        if (bookId != null) {
            bookRepository.clearActiveWorkerTaskId(bookId);
        }
    }

    @Transactional
    public void incrementGenerationRetryCount(UUID bookId) {
        if (bookId != null) {
            bookRepository.incrementGenerationRetryCount(bookId);
        }
    }

    @Transactional
    public void resetGenerationRetryCount(UUID bookId) {
        if (bookId != null) {
            bookRepository.resetGenerationRetryCount(bookId);
        }
    }

    @Transactional
    public void markFileGenerationFailed(UUID bookId) {
        if (bookId != null) {
            bookRepository.markFileGenerationFailed(bookId);
        }
    }

    /**
     * Deletes attachment file AFTER the successful commit from the transaction it was called from.
     */
    private void deleteAttachmentFilesAsync(UUID bookId, Set<UUID> attachmentIds) {
        attachmentIds.forEach(attachmentId -> {
            asyncUtils.runAsyncAfterCommitNoTransaction(() -> {
                try {
                    attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(attachmentId);
                } catch (Exception e) {
                    log.error("Failed to delete format reference attachment file {} during book {}", attachmentId, bookId, e);
                }
            });
        });
    }
}
