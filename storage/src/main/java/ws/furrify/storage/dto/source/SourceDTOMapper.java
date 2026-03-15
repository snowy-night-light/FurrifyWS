package ws.furrify.storage.dto.source;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.source.Source;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface SourceDTOMapper extends BaseDTOMapper<Source, SourceDTO> {
    @Override
    void patchDTO(@MappingTarget SourceDTO source, SourceDTO patchDto);

    @Override
    Source toEntity(SourceDTO dto);

    @Override
    SourceDTO toDto(Source entity);
}