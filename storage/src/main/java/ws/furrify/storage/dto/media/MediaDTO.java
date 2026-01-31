package ws.furrify.storage.dto.media;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaDTO extends BaseEntityDTO<Media> {
}
