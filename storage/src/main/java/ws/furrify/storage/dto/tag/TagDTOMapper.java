package ws.furrify.storage.dto.tag;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.alias.TagAliasDTOMapper;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagAliasDTOMapper.class, TagCategoryDTOMapper.class}
)
public interface TagDTOMapper extends BaseDTOMapper<Tag, TagDTO, PatchTagRequest> {
}