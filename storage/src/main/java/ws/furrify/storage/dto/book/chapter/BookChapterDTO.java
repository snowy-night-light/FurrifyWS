package ws.furrify.storage.dto.book.chapter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookChapterDTO extends UserScopedEntityDTO<BookChapter> {
    private String title;

    private BookDTO book;

    private Long views;
    private Long currentNumberOfWords;

    private List<SourceDTO> sources;
    private List<BookChapterVersionDTO> versions;
}
