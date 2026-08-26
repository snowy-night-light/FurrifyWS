package ws.furrify.storage.dto.tag.alias.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;

@Data
public class CreateTagAliasRequest implements BaseCreateEntityRequest<TagAlias, TagAliasDTO> {
    @NotNull
    private EntityIdRequest targetTag;

    @Pattern(regexp = "^[a-z0-9]+(?: [a-z0-9]+)*$")
    private String alias;
}
