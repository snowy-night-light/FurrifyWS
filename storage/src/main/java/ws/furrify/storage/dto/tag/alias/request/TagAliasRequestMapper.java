package ws.furrify.storage.dto.tag.alias.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface TagAliasRequestMapper extends BaseRequestMapper<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> {
    @Override
    TagAliasDTO toDto(PatchTagAliasRequest patchDto);

    @Override
    TagAliasDTO toDto(CreateTagAliasRequest createTagAliasRequest);
}