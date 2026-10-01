package ws.furrify.storage.dto.media;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.request.PatchMediaRequest;
import ws.furrify.storage.dto.source.SourceDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {SourceDTOMapper.class}
)
public interface MediaDTOMapper extends BaseDTOMapper<Media, MediaDTO, PatchMediaRequest> {
}