package ws.furrify.storage.dto.tag;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.alias.TagAliasDTOMapper;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagAliasDTOMapper.class, TagCategoryDTOMapper.class}
)
public interface TagDTOMapper extends BaseDTOMapper<Tag, TagDTO> {
    @Override
    void patchDTO(@MappingTarget TagDTO source, TagDTO patchDto);

    @Override
    Tag toEntity(TagDTO dto);

    @Override
    TagDTO toDto(Tag entity);
}