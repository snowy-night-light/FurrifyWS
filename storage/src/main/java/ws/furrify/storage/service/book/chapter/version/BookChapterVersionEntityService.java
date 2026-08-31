package ws.furrify.storage.service.book.chapter.version;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
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
import ws.furrify.storage.shared.util.ContentHtmlSanitizerUtil;

import java.time.ZonedDateTime;
import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@Service
public class BookChapterVersionEntityService extends BaseEntityCrudService<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> {

    private final BookChapterEntityService bookChapterEntityService;
    @Autowired
    public BookChapterVersionEntityService(BaseEntityRepository<BookChapterVersion> entityRepository, BaseDTOMapper<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> dtoMapper, @Lazy BookChapterEntityService bookChapterEntityService) {
        super(entityRepository, dtoMapper);
        this.bookChapterEntityService = bookChapterEntityService;
    }

    @Override
    @Transactional
    public BookChapterVersionDTO patchById(UUID id, PatchBookChapterVersionRequest patchDto) {
        this.handleInternalReference(patchDto.getChapter(), bookChapterEntityService);

        // Sanitize content
        if (patchDto.getContentHtml().isPresent()) {
            patchDto.setContentHtml(JsonNullable.of(sanitizeHtml(patchDto.getContentHtml().get())));
        }

        // If content is updated, update the contentUpdatedAt field unless passed directly in dto
        if (!patchDto.getContentUpdatedAt().isPresent() && patchDto.getContentHtml().isPresent()) {
            patchDto.setContentUpdatedAt(JsonNullable.of(ZonedDateTime.now()));
        }

        BookChapterVersionDTO bookChapterVersionDTO = super.patchById(id, patchDto);
        this.countChapterWordsAsync(bookChapterVersionDTO);

        return bookChapterVersionDTO;
    }

    @Override
    @Transactional
    public BookChapterVersionDTO create(BookChapterVersionDTO dto) {
        this.handleInternalReference(dto, BookChapterVersionDTO::getChapter, BookChapterVersionDTO::setChapter, this.bookChapterEntityService);

        int highestVersion = this.getHighestChapterVersion(dto.getChapter().getId());
        dto.setChapterVersion(highestVersion + 1);

        // Sanitize content
        dto.setContentHtml(sanitizeHtml(dto.getContentHtml()));

        // Set content updated at unless passed directly in dto
        if(dto.getContentUpdatedAt() == null) {
             dto.setContentUpdatedAt(ZonedDateTime.now());
        }

        BookChapterVersionDTO createdDto = super.create(dto);
        this.countChapterWordsAsync(createdDto);

        return createdDto;
    }

    @Async
    @Transactional
    protected void countChapterWordsAsync(BookChapterVersionDTO bookChapterVersionDTO) {
        String content = bookChapterVersionDTO.getContentHtml();

        long wordCount = 0;

        boolean word = false;
        int endOfLine = content.length() - 1;

        for (int i = 0; i < content.length(); i++) {
            if (Character.isLetter(content.charAt(i)) && i != endOfLine) {
                word = true;
            } else if (!Character.isLetter(content.charAt(i)) && word) {
                wordCount++;
                word = false;
            } else if (Character.isLetter(content.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }

        bookChapterVersionDTO.setWordCount(wordCount);

        this.internalPutById(bookChapterVersionDTO.getId(), bookChapterVersionDTO);

        this.bookChapterEntityService.updateChapterCurrentWordCountAsync(bookChapterVersionDTO.getChapter().getId());
    }

    private String sanitizeHtml(String html) {
        return ContentHtmlSanitizerUtil.sanitize(html);
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
