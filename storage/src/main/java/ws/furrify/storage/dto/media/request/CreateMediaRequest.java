package ws.furrify.storage.dto.media.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

import java.util.List;
import java.util.UUID;

@Data
public class CreateMediaRequest implements BaseCreateEntityRequest<Media, MediaDTO> {
    @NotNull
    @Range(min = 0)
    private Integer priority;

    @NotNull
    private UUID fileReferenceId;

    @NotNull
    private List<@NotNull EntityIdRequest> sources;
}
