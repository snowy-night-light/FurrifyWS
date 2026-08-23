package ws.furrify.storage.dto.book.chapter.version.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;

@Data
public class CreateBookChapterVersionRequest implements BaseCreateEntityRequest<BookChapterVersion, BookChapterVersionDTO> {

    @NotNull
    private String content;

    private String authorNotesEnd;
    private String authorNotesStart;

    @NotNull
    private EntityIdRequest chapter;
}
