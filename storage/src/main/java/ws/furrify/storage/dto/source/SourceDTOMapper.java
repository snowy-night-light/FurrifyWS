package ws.furrify.storage.dto.source;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.request.PatchSourceRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface SourceDTOMapper extends BaseDTOMapper<Source, SourceDTO, PatchSourceRequest> {
}