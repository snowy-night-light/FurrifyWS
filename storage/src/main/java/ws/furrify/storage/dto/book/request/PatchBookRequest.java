package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;

import java.util.List;

@Data
public class PatchBookRequest implements BasePatchEntityRequest<Book, BookDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();


    private JsonNullable<@NotBlank String> description = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> cover = JsonNullable.undefined();
    private JsonNullable<@NotNull EntityIdRequest> status = JsonNullable.undefined();
    private JsonNullable<@NotNull EntityIdRequest> rating = JsonNullable.undefined();
    private JsonNullable<@NotNull EntityIdRequest> library = JsonNullable.undefined();
    private JsonNullable<@NotNull Integer> likes = JsonNullable.undefined();
    private JsonNullable<@NotNull Integer> dislikes = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> tags = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> artists = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
}
