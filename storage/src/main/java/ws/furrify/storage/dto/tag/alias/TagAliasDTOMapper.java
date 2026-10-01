package ws.furrify.storage.dto.tag.alias;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagCategoryDTOMapper.class}
)
public interface TagAliasDTOMapper extends BaseDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequest> {

    @Override
    @Mapping(target = "targetTag", qualifiedByName = "tagToTagDtoWithoutAliases")
    TagAliasDTO toDto(TagAlias entity, @Context CycleAvoidingMappingContext context);

    @Named("tagToTagDtoWithoutAliases")
    @Mapping(target = "aliases", ignore = true)
    TagDTO tagToTagDtoWithoutAliases(Tag tag, @Context CycleAvoidingMappingContext context);

    @Named("tagAliasToTagAliasDtoWithoutTargetTag")
    @Mapping(target = "targetTag", ignore = true)
    TagAliasDTO tagAliasToTagAliasDtoWithoutTargetTag(TagAlias entity, @Context CycleAvoidingMappingContext context);
}