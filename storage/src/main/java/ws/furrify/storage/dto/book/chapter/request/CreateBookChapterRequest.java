package ws.furrify.storage.dto.book.chapter.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

@Data
public class CreateBookChapterRequest implements BaseCreateEntityRequest<BookChapter, BookChapterDTO> {

    @NotBlank
    private String title;

    @NotNull
    private EntityIdRequest book;
}
