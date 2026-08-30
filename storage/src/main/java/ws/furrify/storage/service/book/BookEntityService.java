package ws.furrify.storage.service.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
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

import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@Service
public class BookEntityService extends BaseEntityCrudService<Book, BookDTO, PatchBookRequest> {

    private final MediaEntityService mediaEntityService;
    private final BookChapterEntityService bookChapterEntityService;
    private final TagEntityService tagEntityService;
    private final ArtistEntityService artistEntityService;
    private final LibraryEntityService libraryEntityService;
    private final SourceEntityService sourceEntityService;

    @Autowired
    public BookEntityService(BaseEntityRepository<Book> entityRepository, BaseDTOMapper<Book, BookDTO, PatchBookRequest> dtoMapper, MediaEntityService mediaEntityService, BookChapterEntityService bookChapterEntityService, TagEntityService tagEntityService, ArtistEntityService artistEntityService, LibraryEntityService libraryEntityService, SourceEntityService sourceEntityService) {
        super(entityRepository, dtoMapper);
        this.mediaEntityService = mediaEntityService;
        this.bookChapterEntityService = bookChapterEntityService;
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.libraryEntityService = libraryEntityService;
        this.sourceEntityService = sourceEntityService;
    }

    @Override
    public BookDTO create(BookDTO dto) {
        this.handleInternalReference(dto, BookDTO::getCover, BookDTO::setCover, mediaEntityService);
        this.handleInternalReference(dto, BookDTO::getLibrary, BookDTO::setLibrary, libraryEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getTags, BookDTO::setTags, tagEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getArtists, BookDTO::setArtists, artistEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getSources, BookDTO::setSources, sourceEntityService);

        LibraryDTO libraryDTO = dto.getLibrary();
        checkLikesEnabled(libraryDTO, dto.getLikes(), dto.getDislikes());

        return super.create(dto);
    }

    @Override
    public BookDTO patchById(UUID id, PatchBookRequest patchDto) {
        this.handleInternalReference(patchDto.getCover(), mediaEntityService);
        this.handleInternalReference(patchDto.getLibrary(), libraryEntityService);
        this.handleCollectionInternalReferences(patchDto.getTags(), tagEntityService);
        this.handleCollectionInternalReferences(patchDto.getArtists(), artistEntityService);
        this.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        LibraryDTO libraryDTO = super.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id))).getLibrary();
        checkLikesEnabled(libraryDTO, patchDto.getLikes().orElse(null), patchDto.getDislikes().orElse(null));

        return super.patchById(id, patchDto);
    }

    @Override
    public void deleteById(UUID id) {
        this.mediaEntityService.deleteById(id);

        super.deleteById(id);
    }

    private void checkLikesEnabled(LibraryDTO libraryDTO, Integer likes, Integer dislikes) {
        if (dislikes != null && !libraryDTO.getDislikesEnabled()) {
            throw new ServiceLogicException(StorageErrors.DISLIKES_DISABLED_EXCEPTION.getErrorMessage(libraryDTO.getId()));
        }

        if (likes != null && !libraryDTO.getLikesEnabled()) {
            throw new ServiceLogicException(StorageErrors.LIKES_DISABLED_EXCEPTION.getErrorMessage(libraryDTO.getId()));
        }
    }

    @Async
    @Transactional
    public void updateBookTotalWordCountAsync(UUID bookId) {
        BookDTO bookDTO = this.findById(bookId).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(bookId)));

        EntitySpecResult<BookChapter> entitySpecResult = EntitySpec.<BookChapter>specBuilder().where("book.id", specEquals(bookId)).build();

        Page<BookChapterDTO> bookChapters = this.bookChapterEntityService.getAllPaged(entitySpecResult.specString(), PageRequest.of(0, 100));
        Long bookWordCount = bookChapters.get()
                .map(BookChapterDTO::getCurrentNumberOfWords)
                .reduce(0L, Long::sum);

        bookDTO.setTotalWordCount(bookWordCount);

        this.internalPutById(bookId, bookDTO);
    }

}
