package ws.furrify.storage.dto.tag.category;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.tag.category.TagCategory;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TagCategoryDTOMapper extends BaseEntityDTOMapper<TagCategory, TagCategoryDTO, PatchTagCategoryRequestDTO> {
}