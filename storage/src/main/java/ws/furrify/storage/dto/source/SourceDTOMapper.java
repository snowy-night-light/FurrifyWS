package ws.furrify.storage.dto.source;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.source.Source;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface SourceDTOMapper extends BaseDTOMapper<Source, SourceDTO> {
}