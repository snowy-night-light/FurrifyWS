package ws.furrify.storage.dto.tag.alias;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.tag.alias.TagAlias;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TagAliasDTOMapper extends BaseEntityDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequestDTO> {
}