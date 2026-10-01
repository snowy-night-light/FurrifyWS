package ws.furrify.storage.dto.collection.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;

import java.util.List;

@Data
public class CreateCollectionRequest implements BaseCreateEntityRequest<Collection, CollectionDTO> {

    @Length(max = 120)
    private String title;

    private List<@NotNull EntityIdRequest> posts;
    private EntityIdRequest library;
}
