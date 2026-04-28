package ws.furrify.storage.dto.media.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

import java.util.List;
import java.util.UUID;

@Data
public class PatchMediaRequest implements BasePatchEntityRequest<Media, MediaDTO> {
    private JsonNullable<@NotNull @Range(min = 0) Integer> priority;

    private JsonNullable<@NotNull UUID> fileReferenceId;

    private JsonNullable<List<@NotNull EntityIdRequest>> sources;
}
