package ws.furrify.storage.dto.post;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.tag.TagDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class PostDTO extends UserScopedEntityDTO<Post> {
    private String title;
    private String description;

    private List<TagDTO> tags;
    private List<ArtistDTO> artists;

    private List<MediaDTO> displayMediaList;
    private List<MediaDTO> attachments;
}
