package ws.furrify.storage.dto.book.chapter.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PatchBookChapterRequest implements BasePatchEntityRequest<BookChapter, BookChapterDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> externalId = JsonNullable.undefined();
    private JsonNullable<@NotNull @Positive Integer> chapterNumber = JsonNullable.undefined();
    private JsonNullable<@NotNull @PositiveOrZero Long> views = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> book = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> publishDate = JsonNullable.undefined();
}
