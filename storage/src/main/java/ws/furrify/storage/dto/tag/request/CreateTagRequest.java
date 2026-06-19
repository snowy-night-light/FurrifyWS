package ws.furrify.storage.dto.tag.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

import java.util.List;

@Data
public class CreateTagRequest implements BaseCreateEntityRequest<Tag, TagDTO> {

    private List<@NotNull EntityIdRequest> aliases;

    @NotNull
    private EntityIdRequest category;

    @Pattern(regexp = "^[a-z0-9 ]+$")
    private String name;

}
