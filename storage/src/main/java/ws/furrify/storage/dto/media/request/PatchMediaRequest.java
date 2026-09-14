package ws.furrify.storage.dto.media.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PatchMediaRequest implements BasePatchEntityRequest<Media, MediaDTO> {
    private JsonNullable<@NotNull @Min(0) Integer> priority = JsonNullable.undefined();

    private JsonNullable<@NotNull UUID> fileReferenceId = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
    private JsonNullable<@NotNull ZonedDateTime> externalUpdatedAt = JsonNullable.undefined();
}
