package ws.furrify.storage.dto.tag.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface TagRequestMapper extends BaseRequestMapper<Tag, TagDTO, CreateTagRequest, PatchTagRequest> {
    @Override
    TagDTO toDto(PatchTagRequest patchRequest);

    @Override
    TagDTO toDto(CreateTagRequest createRequest);
}