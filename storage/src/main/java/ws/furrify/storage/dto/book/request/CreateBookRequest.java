package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;

@Data
public class CreateBookRequest implements BaseCreateEntityRequest<Book, BookDTO> {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private EntityIdRequest cover;
    private EntityIdRequest library;
}
