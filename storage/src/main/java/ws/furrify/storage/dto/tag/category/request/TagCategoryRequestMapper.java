package ws.furrify.storage.dto.tag.category.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface TagCategoryRequestMapper extends BaseRequestMapper<TagCategory, TagCategoryDTO, CreateTagCategoryRequest, PatchTagCategoryRequest> {
    @Override
    TagCategoryDTO toDto(PatchTagCategoryRequest patchDto);

    @Override
    TagCategoryDTO toDto(CreateTagCategoryRequest createTagCategoryRequest);
}