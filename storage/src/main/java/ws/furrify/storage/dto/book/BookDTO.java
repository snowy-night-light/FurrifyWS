package ws.furrify.storage.dto.book;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.tag.TagDTO;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookDTO extends UserScopedEntityDTO<Book> {
    private String title;

    private String externalId;
    private Integer chapterNumber;

    private String descriptionHtml;
    private String shortDescriptionHtml;

    private MediaDTO cover;
    private BookDTO prequel;
    private BookDTO sequel;

    private Long totalWordCount;

    private Long views;
    private Integer likes;
    private Integer dislikes;

    private BookStatus status;
    private BookRating rating;

    private List<BookChapterDTO> chapters;

    private List<TagDTO> tags;
    private List<ArtistDTO> artists;
    private List<SourceDTO> sources;

    private LibraryDTO library;

    private ZonedDateTime publishDate;
}
