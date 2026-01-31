package ws.furrify.storage.dto.source;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.source.Source;

@EqualsAndHashCode(callSuper = true)
@Data
public class SourceDTO extends BaseEntityDTO<Source> {
}
