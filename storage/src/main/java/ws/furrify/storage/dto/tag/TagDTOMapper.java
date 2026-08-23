package ws.furrify.storage.dto.tag;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.library.LibraryDTOMapper;
import ws.furrify.storage.dto.tag.alias.TagAliasDTOMapper;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagAliasDTOMapper.class, TagCategoryDTOMapper.class, LibraryDTOMapper.class}
)
public interface TagDTOMapper extends BaseDTOMapper<Tag, TagDTO, PatchTagRequest> {

    @Override
    @Mapping(target = "aliases", qualifiedByName = "tagAliasToTagAliasDtoWithoutTargetTag")
    TagDTO toDto(Tag entity, @Context CycleAvoidingMappingContext context);
}