package ws.furrify.storage.dto.book.chapter.version.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;

@Data
public class PatchBookChapterVersionRequest implements BasePatchEntityRequest<BookChapterVersion, BookChapterVersionDTO> {
    private JsonNullable<@NotNull String> content = JsonNullable.undefined();

    private JsonNullable<String> authorNotesEnd = JsonNullable.undefined();
    private JsonNullable<String> authorNotesStart = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> chapter = JsonNullable.undefined();
}
