package ws.furrify.storage.dto.tag.alias;

import org.mapstruct.*;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

@Mapper(
        config = BaseDTOMapper.class
)
public interface TagAliasDTOMapper extends BaseDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequest> {

    @Override
    @Mapping(target = "targetTag", ignore = true)
    TagAliasDTO toDto(TagAlias entity, CycleAvoidingMappingContext context);

    @AfterMapping
    default void linkBackReference(TagAlias entity, @MappingTarget TagAliasDTO dto, @Context CycleAvoidingMappingContext context) {
        if (entity.getTargetTag() != null) {
            dto.setTargetTag(context.getMappedInstance(entity.getTargetTag(), TagDTO.class));
        }
    }
}