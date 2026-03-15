package ws.furrify.storage.dto.tag.alias;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.TagDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagDTOMapper.class}
)
public interface TagAliasDTOMapper extends BaseDTOMapper<TagAlias, TagAliasDTO> {
    @Override
    void patchDTO(@MappingTarget TagAliasDTO source, TagAliasDTO patchDto);

    @Override
    TagAlias toEntity(TagAliasDTO dto);

    @Override
    TagAliasDTO toDto(TagAlias entity);
}