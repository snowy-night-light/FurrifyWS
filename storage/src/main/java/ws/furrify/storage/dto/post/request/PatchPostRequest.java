package ws.furrify.storage.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;

import java.util.List;

@Data
public class PatchPostRequest implements BasePatchEntityRequest<Post, PostDTO> {

    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> description = JsonNullable.undefined();

    private JsonNullable<@NotEmpty List<@NotNull EntityIdRequest>> tags = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> artists = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> displayMediaList = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> attachments = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> library = JsonNullable.undefined();
}
