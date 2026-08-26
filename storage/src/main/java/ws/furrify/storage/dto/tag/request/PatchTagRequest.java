package ws.furrify.storage.dto.tag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

import java.util.List;

@Data
public class PatchTagRequest implements BasePatchEntityRequest<Tag, TagDTO> {

    private JsonNullable<List<@NotNull EntityIdRequest>> aliases = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> category = JsonNullable.undefined();

    private JsonNullable<@NotBlank @Pattern(regexp = "^[a-z0-9]+(?: [a-z0-9]+)*$") String> name = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> library = JsonNullable.undefined();
}
