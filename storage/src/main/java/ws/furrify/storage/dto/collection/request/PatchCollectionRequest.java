package ws.furrify.storage.dto.collection.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;

import java.util.List;

@Data
public class PatchCollectionRequest implements BasePatchEntityRequest<Collection, CollectionDTO> {

    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> posts = JsonNullable.undefined();

}
