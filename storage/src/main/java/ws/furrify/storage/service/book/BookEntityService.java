package ws.furrify.storage.service.book;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.service.artist.ArtistEntityService;
import ws.furrify.storage.service.book.chapter.BookChapterEntityService;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.service.tag.TagEntityService;
import ws.furrify.storage.shared.exception.StorageErrors;
import ws.furrify.storage.shared.util.ContentHtmlSanitizerUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@Service
@Slf4j
public class BookEntityService extends BaseEntityCrudService<Book, BookDTO, PatchBookRequest> {

    private final MediaEntityService mediaEntityService;
    private final BookChapterEntityService bookChapterEntityService;
    private final TagEntityService tagEntityService;
    private final ArtistEntityService artistEntityService;
    private final LibraryEntityService libraryEntityService;
    private final SourceEntityService sourceEntityService;
    private final BookFileGenerationService bookFileGenerationService;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final AsyncUtils asyncUtils;

    @Autowired
    public BookEntityService(BaseEntityRepository<Book> entityRepository, BaseDTOMapper<Book, BookDTO, PatchBookRequest> dtoMapper, MediaEntityService mediaEntityService, BookChapterEntityService bookChapterEntityService, TagEntityService tagEntityService, ArtistEntityService artistEntityService, LibraryEntityService libraryEntityService, SourceEntityService sourceEntityService, @Lazy BookFileGenerationService bookFileGenerationService, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, AsyncUtils asyncUtils) {
        super(entityRepository, dtoMapper);
        this.mediaEntityService = mediaEntityService;
        this.bookChapterEntityService = bookChapterEntityService;
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.libraryEntityService = libraryEntityService;
        this.sourceEntityService = sourceEntityService;
        this.bookFileGenerationService = bookFileGenerationService;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.asyncUtils = asyncUtils;
    }

    @Override
    public BookDTO create(BookDTO dto) {
        this.handleInternalReference(dto, BookDTO::getCover, BookDTO::setCover, mediaEntityService);
        this.handleInternalReference(dto, BookDTO::getLibrary, BookDTO::setLibrary, libraryEntityService);
        this.handleInternalReference(dto, BookDTO::getSequel, BookDTO::setSequel, this);
        this.handleInternalReference(dto, BookDTO::getPrequel, BookDTO::setPrequel, this);
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

        return super.create(dto);
    }

    @Override
    public BookDTO patchById(UUID id, PatchBookRequest patchDto) {
        this.handleInternalReference(patchDto.getCover(), mediaEntityService);
        this.handleInternalReference(patchDto.getSequel(), this);
        this.handleInternalReference(patchDto.getPrequel(), this);
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

        // Verify user can update likes and dislikes based on library setting
        LibraryDTO libraryDTO = super.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id))).getLibrary();
        checkLikesEnabled(libraryDTO, patchDto.getLikes().orElse(null), patchDto.getDislikes().orElse(null));

        boolean needsRegeneration = patchDto.getTitle().isPresent() ||
                patchDto.getDescriptionHtml().isPresent() ||
                patchDto.getShortDescriptionHtml().isPresent() ||
                patchDto.getArtists().isPresent() ||
                patchDto.getTags().isPresent() ||
                patchDto.getCover().isPresent();

        BookDTO patchedBook = super.patchById(id, patchDto);

        if (needsRegeneration) {
            bookFileGenerationService.scheduleGeneration(id);
        }

        return patchedBook;
    }

    @Override
    public void deleteById(UUID id) {
        BookDTO bookDTO = super.findById(id).orElse(null);
        if (bookDTO != null && bookDTO.getFormatReferenceIds() != null && !bookDTO.getFormatReferenceIds().isEmpty()) {
            bookDTO.getFormatReferenceIds().values().forEach(attachmentId -> {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(attachmentId);
                    } catch (Exception e) {
                        log.error("Failed to delete format reference attachment file {} during book {} deletion", attachmentId, id, e);
                    }
                });
            });
        }

        this.mediaEntityService.deleteById(id);

        super.deleteById(id);
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
    public void updateBookTotalWordCountAsync(UUID bookId) {
        BookDTO bookDTO = this.findById(bookId).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(bookId)));

        EntitySpecResult<BookChapter> entitySpecResult = EntitySpec.<BookChapter>specBuilder().where("book.id", specEquals(bookId)).build();

        Page<BookChapterDTO> bookChapters = this.bookChapterEntityService.getAllPaged(entitySpecResult.specString(), PageRequest.of(0, 100));
        Long bookWordCount = bookChapters.get()
                .map(c -> c.getCurrentNumberOfWords() != null ? c.getCurrentNumberOfWords() : 0L)
                .reduce(0L, Long::sum);

        bookDTO.setTotalWordCount(bookWordCount);

        this.internalPutById(bookId, bookDTO);
    }

    @Transactional
    public void markNeedsGeneration(UUID bookId, boolean needsGeneration) {
        BookDTO book = this.findById(bookId).orElse(null);
        if (book != null) {
            book.setNeedsBookFileGeneration(needsGeneration);
            this.internalPutById(bookId, book);
        }
    }

    @Transactional
    public void updateBookFileWorkerTaskInfo(UUID bookId, Map<String, UUID> newFormatReferenceIds, UUID newActiveWorkerTaskId) {
        BookDTO book = this.findById(bookId).orElse(null);
        if (book != null) {
            if (newFormatReferenceIds != null) {
                if (book.getFormatReferenceIds() != null) {
                    Set<UUID> oldAttachments = new HashSet<>(book.getFormatReferenceIds().values());
                    Set<UUID> newAttachments = new HashSet<>(newFormatReferenceIds.values());
                    
                    oldAttachments.removeAll(newAttachments);
                    
                    deleteAttachmentFilesAsync(bookId, oldAttachments);
                    book.getFormatReferenceIds().clear();
                    book.getFormatReferenceIds().putAll(newFormatReferenceIds);
                } else {
                    book.setFormatReferenceIds(newFormatReferenceIds);
                }
            }
            book.setActiveWorkerTaskId(newActiveWorkerTaskId);
            this.internalPutById(bookId, book);
        }
    }

    @Transactional
    public void assignWorkerTask(UUID bookId, UUID workerTaskId) {
        BookDTO book = this.findById(bookId).orElse(null);
        if (book != null) {
            book.setActiveWorkerTaskId(workerTaskId);
            if (book.getFormatReferenceIds() != null) {
                deleteAttachmentFilesAsync(bookId, new HashSet<>(book.getFormatReferenceIds().values()));
                book.getFormatReferenceIds().clear();
            }
            book.setNeedsBookFileGeneration(false);
            this.internalPutById(bookId, book);
        }
    }

    /**
     * Deletes attachment file AFTER the successful commit from the transaction it was called from.
     */
    private void deleteAttachmentFilesAsync(UUID bookId, Set<UUID> attachmentIds) {
        attachmentIds.forEach(attachmentId -> {
            asyncUtils.runAsyncAfterCommit(() -> {
                try {
                    attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(attachmentId);
                } catch (Exception e) {
                    log.error("Failed to delete format reference attachment file {} during book {}", attachmentId, bookId, e);
                }
            });
        });
    }
}
