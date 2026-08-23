package ws.furrify.storage.service.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;

import java.util.UUID;

@Service
public class BookEntityService extends BaseEntityCrudService<Book, BookDTO, PatchBookRequest> {

    private final MediaEntityService mediaEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public BookEntityService(BaseEntityRepository<Book> entityRepository, BaseDTOMapper<Book, BookDTO, PatchBookRequest> dtoMapper, MediaEntityService mediaEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.mediaEntityService = mediaEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    public BookDTO create(BookDTO dto) {
        this.handleCreateInternalReference(dto, BookDTO::getCover, BookDTO::setCover, mediaEntityService);
        this.handleCreateInternalReference(dto, BookDTO::getLibrary, BookDTO::setLibrary, libraryEntityService);

        return super.create(dto);
    }

    @Override
    public BookDTO patchById(UUID id, PatchBookRequest patchDto) {
        this.handlePatchInternalReference(patchDto.getCover(), mediaEntityService);
        this.handlePatchInternalReference(patchDto.getLibrary(), mediaEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public void deleteById(UUID id) {
        this.mediaEntityService.deleteById(id);

        super.deleteById(id);
    }
}
