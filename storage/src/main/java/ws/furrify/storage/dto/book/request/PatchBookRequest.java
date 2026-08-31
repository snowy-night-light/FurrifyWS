package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PatchBookRequest implements BasePatchEntityRequest<Book, BookDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> externalId = JsonNullable.undefined();

    private JsonNullable<@NotNull @Length(max = 10240) String> descriptionHtml = JsonNullable.undefined();
    private JsonNullable<@NotNull @Length(max = 1024) String> shortDescriptionHtml = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> cover = JsonNullable.undefined();
    private JsonNullable<EntityIdRequest> sequel = JsonNullable.undefined();
    private JsonNullable<EntityIdRequest> prequel = JsonNullable.undefined();
    private JsonNullable<@NotNull BookStatus> status = JsonNullable.undefined();
    private JsonNullable<@NotNull BookRating> rating = JsonNullable.undefined();
    private JsonNullable<@NotNull EntityIdRequest> library = JsonNullable.undefined();
    private JsonNullable<@NotNull Integer> likes = JsonNullable.undefined();
    private JsonNullable<@NotNull Integer> dislikes = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> tags = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> artists = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
    private JsonNullable<@NotNull ZonedDateTime> publishDate = JsonNullable.undefined();
}
