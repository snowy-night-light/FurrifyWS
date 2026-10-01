package ws.furrify.storage.dto.book.chapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookChapterDTO extends UserScopedEntityDTO<BookChapter> {
    private String title;

    private String externalId;
    private Integer chapterNumber;

    @JsonIgnoreProperties("chapters")
    private BookDTO book;

    private Long views;
    private Long currentNumberOfWords;

    private List<SourceDTO> sources;
    @JsonIgnoreProperties("chapter")
    private List<BookChapterVersionDTO> versions;
    private Integer versionsCount;

    private ZonedDateTime publishDate;
}
