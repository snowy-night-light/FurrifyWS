package ws.furrify.storage.dto.media.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface MediaRequestMapper extends BaseRequestMapper<Media, MediaDTO, CreateMediaRequest> {
    @Override
    MediaDTO toDto(CreateMediaRequest createMediaRequest);
}
