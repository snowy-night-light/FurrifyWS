package ws.furrify.storage.service.book.chapter.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;
import ws.furrify.storage.service.book.chapter.BookChapterEntityService;

import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@Service
public class BookChapterVersionEntityService extends BaseEntityCrudService<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> {

    private final BookChapterEntityService bookChapterEntityService;
    @Autowired
    public BookChapterVersionEntityService(BaseEntityRepository<BookChapterVersion> entityRepository, BaseDTOMapper<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> dtoMapper, BookChapterEntityService bookChapterEntityService) {
        super(entityRepository, dtoMapper);
        this.bookChapterEntityService = bookChapterEntityService;
    }

    @Override
    public BookChapterVersionDTO patchById(UUID id, PatchBookChapterVersionRequest patchDto) {
        this.handlePatchInternalReference(patchDto.getChapter(), bookChapterEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    @Transactional
    public BookChapterVersionDTO create(BookChapterVersionDTO dto) {
        this.handleCreateInternalReference(dto, BookChapterVersionDTO::getChapter, BookChapterVersionDTO::setChapter, this.bookChapterEntityService);

        int highestVersion = this.getHighestChapterVersion(dto.getChapter().getId());
        dto.setChapterVersion(highestVersion + 1);

        return super.create(dto);
    }

    @Transactional
    protected int getHighestChapterVersion(UUID chapterId) {
        EntitySpecResult<BookChapterVersion> chapterVersionsSpec = EntitySpec.from(SecurityContextUtils.<BookChapterVersion>getUserScopedSecuritySpec())
                .and().where("chapter_id", specEquals(chapterId))
                .build();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "chapterVersion"));

        Page<BookChapterVersionDTO> bookChapterVersions = this.getAllPaged(chapterVersionsSpec.specString(), pageable);

        if (bookChapterVersions.isEmpty() || bookChapterVersions.getContent().isEmpty()) {
            return 0;
        } else {
            return bookChapterVersions.getContent().getFirst().getChapterVersion();
        }
    }
}
