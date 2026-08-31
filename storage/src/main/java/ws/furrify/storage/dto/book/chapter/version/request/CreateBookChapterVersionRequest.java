package ws.furrify.storage.dto.book.chapter.version.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;

import java.time.ZonedDateTime;

@Data
public class CreateBookChapterVersionRequest implements BaseCreateEntityRequest<BookChapterVersion, BookChapterVersionDTO> {

    @NotNull
    private String contentHtml;
    private String contentStylesheet;

    private String authorNotesEnd;
    private String authorNotesStart;

    @NotNull
    private EntityIdRequest chapter;

    private ZonedDateTime contentUpdatedAt;
}
