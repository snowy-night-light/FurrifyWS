package ws.furrify.storage.dto.tag.alias.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;

import java.util.List;

@Data
public class PatchTagAliasRequest implements BasePatchEntityRequest<TagAlias, TagAliasDTO> {

    private JsonNullable<@NotNull EntityIdRequest> targetTag = JsonNullable.undefined();

    private JsonNullable<@NotBlank @Pattern(regexp = "^[a-z0-9 ]+$") String> alias = JsonNullable.undefined();
}
