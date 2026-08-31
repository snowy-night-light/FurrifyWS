package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateBookRequest implements BaseCreateEntityRequest<Book, BookDTO> {

    private String externalId;

    @NotBlank
    private String title;

    @Length(max = 10240)
    @NotNull
    private String descriptionHtml;

    @Length(max = 1024)
    @NotNull
    private String shortDescriptionHtml;

    private EntityIdRequest cover;
    private EntityIdRequest sequel;
    private EntityIdRequest prequel;
    @NotNull
    private BookStatus status;
    @NotNull
    private BookRating rating;
    @NotNull
    private Long views;
    private Integer likes;
    private Integer dislikes;
    @NotNull
    private EntityIdRequest library;
    private List<@NotNull EntityIdRequest> tags;
    private List<@NotNull EntityIdRequest> artists;
    private List<@NotNull EntityIdRequest> sources;

    @NotNull
    private ZonedDateTime publishDate;
}
