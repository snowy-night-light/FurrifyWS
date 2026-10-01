package ws.furrify.storage.dto.book.chapter.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateBookChapterRequest implements BaseCreateEntityRequest<BookChapter, BookChapterDTO> {

    private String externalId;

    @NotBlank
    private String title;

    @NotNull
    @PositiveOrZero
    private Long views;

    @NotNull
    private EntityIdRequest book;

    private List<@NotNull EntityIdRequest> sources;

    @Positive
    @NotNull
    private Integer chapterNumber;

    private ZonedDateTime publishDate;
}
