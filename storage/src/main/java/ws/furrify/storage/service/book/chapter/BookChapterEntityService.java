package ws.furrify.storage.service.book.chapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.service.book.BookEntityService;
import ws.furrify.storage.service.book.chapter.version.BookChapterVersionEntityService;
import ws.furrify.storage.service.source.SourceEntityService;

import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@Service
public class BookChapterEntityService extends BaseEntityCrudService<BookChapter, BookChapterDTO, PatchBookChapterRequest> {

    private final BookEntityService bookEntityService;
    private final BookChapterVersionEntityService bookChapterVersionEntityService;
    private final SourceEntityService sourceEntityService;

    @Autowired
    public BookChapterEntityService(BaseEntityRepository<BookChapter> entityRepository, BaseDTOMapper<BookChapter, BookChapterDTO, PatchBookChapterRequest> dtoMapper, @Lazy BookEntityService bookEntityService, BookChapterVersionEntityService bookChapterVersionEntityService, SourceEntityService sourceEntityService) {
        super(entityRepository, dtoMapper);
        this.bookEntityService = bookEntityService;
        this.bookChapterVersionEntityService = bookChapterVersionEntityService;
        this.sourceEntityService = sourceEntityService;
    }

    @Override
    public BookChapterDTO create(BookChapterDTO dto) {
        this.handleInternalReference(dto, BookChapterDTO::getBook, BookChapterDTO::setBook, this.bookEntityService);
        this.handleInternalCollectionReferences(dto, BookChapterDTO::getSources, BookChapterDTO::setSources, sourceEntityService);

        return super.create(dto);
    }

    @Override
    public BookChapterDTO patchById(UUID id, PatchBookChapterRequest patchDto) {
        this.handleInternalReference(patchDto.getBook(), bookEntityService);
        this.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        return super.patchById(id, patchDto);
    }

    @Async
    @Transactional
    public void updateChapterCurrentWordCountAsync(UUID chapterId) {
        BookChapterDTO bookChapterDTO = this.findById(chapterId).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(chapterId)));

        EntitySpecResult<BookChapterVersion> entitySpecResult = EntitySpec.<BookChapterVersion>specBuilder().where("chapter.id", specEquals(chapterId)).build();

        Page<BookChapterVersionDTO> bookChapterVersionDTOList = this.bookChapterVersionEntityService.getAllPaged(entitySpecResult.specString(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "chapterVersion")));
        long wordCount = bookChapterVersionDTOList.get()
                .findFirst()
                .map(BookChapterVersionDTO::getWordCount)
                .orElse(0L);

        bookChapterDTO.setCurrentNumberOfWords(wordCount);

        this.internalPutById(chapterId, bookChapterDTO);

        this.bookEntityService.updateBookTotalWordCountAsync(bookChapterDTO.getBook().getId());
    }

}
