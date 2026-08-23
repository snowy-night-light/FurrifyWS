package ws.furrify.storage.service.book.chapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;
import ws.furrify.storage.service.book.BookEntityService;

import java.util.UUID;

@Service
public class BookChapterEntityService extends BaseEntityCrudService<BookChapter, BookChapterDTO, PatchBookChapterRequest> {

    private final BookEntityService bookEntityService;

    @Autowired
    public BookChapterEntityService(BaseEntityRepository<BookChapter> entityRepository, BaseDTOMapper<BookChapter, BookChapterDTO, PatchBookChapterRequest> dtoMapper, BookEntityService bookEntityService) {
        super(entityRepository, dtoMapper);
        this.bookEntityService = bookEntityService;
    }

    @Override
    public BookChapterDTO create(BookChapterDTO dto) {
        this.handleCreateInternalReference(dto, BookChapterDTO::getBook, BookChapterDTO::setBook, this.bookEntityService);

        return super.create(dto);
    }

    @Override
    public BookChapterDTO patchById(UUID id, PatchBookChapterRequest patchDto) {
        this.handlePatchInternalReference(patchDto.getBook(), bookEntityService);

        return super.patchById(id, patchDto);
    }

}
