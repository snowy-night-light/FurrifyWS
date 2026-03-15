package ws.furrify.storage.dto.post;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.artist.ArtistDTOMapper;
import ws.furrify.storage.dto.media.MediaDTOMapper;
import ws.furrify.storage.dto.tag.TagDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagDTOMapper.class, ArtistDTOMapper.class, MediaDTOMapper.class}
)
public interface PostDTOMapper extends BaseDTOMapper<Post, PostDTO> {
}