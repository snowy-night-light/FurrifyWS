package ws.furrify.storage.dto.tag.category;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.library.LibraryDTOMapper;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {LibraryDTOMapper.class}
)
public interface TagCategoryDTOMapper extends BaseDTOMapper<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> {
}