package ws.furrify.storage.dto.tag;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.tag.Tag;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TagDTOMapper extends BaseEntityDTOMapper<Tag, TagDTO, PatchTagRequestDTO> {
}