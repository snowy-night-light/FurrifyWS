package ws.furrify.storage.dto.book;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.library.LibraryDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookDTO extends UserScopedEntityDTO<Book> {
    private String title;

    private String description;

    private MediaDTO cover;

    private List<BookChapterDTO> chapters;

    private LibraryDTO library;
}
