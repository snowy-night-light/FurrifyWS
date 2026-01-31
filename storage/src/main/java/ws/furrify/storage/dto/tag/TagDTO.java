package ws.furrify.storage.dto.tag;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.tag.Tag;

@EqualsAndHashCode(callSuper = true)
@Data
public class TagDTO extends BaseEntityDTO<Tag> {
}
