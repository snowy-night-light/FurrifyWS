package ws.furrify.storage.dto.post;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.PatchSourceRequestDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface PostDTOMapper extends BaseEntityDTOMapper<Post, PostDTO, PatchPostRequestDTO> {
}