package ws.furrify.storage.dto.media;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.source.SourceDTO;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MediaDTO extends UserScopedEntityDTO<Media> {
    private Integer priority;

    private UUID fileId;

    private List<SourceDTO> sources;
}
