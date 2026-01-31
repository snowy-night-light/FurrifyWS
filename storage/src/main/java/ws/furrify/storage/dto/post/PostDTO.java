package ws.furrify.storage.dto.post;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.source.Source;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostDTO extends BaseEntityDTO<Post> {
}
