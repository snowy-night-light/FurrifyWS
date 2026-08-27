package ws.furrify.storage.service.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.service.artist.ArtistEntityService;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.tag.TagEntityService;

import java.util.UUID;

@Service
public class BookEntityService extends BaseEntityCrudService<Book, BookDTO, PatchBookRequest> {

    private final MediaEntityService mediaEntityService;
    private final TagEntityService tagEntityService;
    private final ArtistEntityService artistEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public BookEntityService(BaseEntityRepository<Book> entityRepository, BaseDTOMapper<Book, BookDTO, PatchBookRequest> dtoMapper, MediaEntityService mediaEntityService, TagEntityService tagEntityService, ArtistEntityService artistEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.mediaEntityService = mediaEntityService;
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    public BookDTO create(BookDTO dto) {
        this.handleInternalReference(dto, BookDTO::getCover, BookDTO::setCover, mediaEntityService);
        this.handleInternalReference(dto, BookDTO::getLibrary, BookDTO::setLibrary, libraryEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getTags, BookDTO::setTags, tagEntityService);
        this.handleInternalCollectionReferences(dto, BookDTO::getArtists, BookDTO::setArtists, artistEntityService);

        return super.create(dto);
    }

    @Override
    public BookDTO patchById(UUID id, PatchBookRequest patchDto) {
        this.handleInternalReference(patchDto.getCover(), mediaEntityService);
        this.handleInternalReference(patchDto.getLibrary(), libraryEntityService);
        this.handleCollectionInternalReferences(patchDto.getTags(), tagEntityService);
        this.handleCollectionInternalReferences(patchDto.getArtists(), artistEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public void deleteById(UUID id) {
        this.mediaEntityService.deleteById(id);

        super.deleteById(id);
    }
}
