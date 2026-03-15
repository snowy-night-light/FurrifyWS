package ws.furrify.storage.dto.tag.category;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.tag.category.TagCategory;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface TagCategoryDTOMapper extends BaseDTOMapper<TagCategory, TagCategoryDTO> {
    @Override
    void patchDTO(@MappingTarget TagCategoryDTO source, TagCategoryDTO patchDto);

    @Override
    TagCategory toEntity(TagCategoryDTO dto);

    @Override
    TagCategoryDTO toDto(TagCategory entity);
}