package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;

@Data
public class PatchBookRequest implements BasePatchEntityRequest<Book, BookDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();


    private JsonNullable<@NotBlank String> description = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> cover = JsonNullable.undefined();
    private JsonNullable<EntityIdRequest> library = JsonNullable.undefined();
}
