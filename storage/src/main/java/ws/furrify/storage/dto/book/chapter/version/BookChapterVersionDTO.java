package ws.furrify.storage.dto.book.chapter.version;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookChapterVersionDTO extends UserScopedEntityDTO<BookChapterVersion> {
    private Integer chapterVersion;

    private String contentHtml;

    private String authorNotesEnd;
    private String authorNotesStart;
    private String contentStylesheet;

    private Long wordCount;

    private BookChapterDTO chapter;

    private ZonedDateTime contentUpdatedAt;
}
