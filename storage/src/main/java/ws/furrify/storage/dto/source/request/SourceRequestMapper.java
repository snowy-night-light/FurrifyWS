package ws.furrify.storage.dto.source.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface SourceRequestMapper extends BaseRequestMapper<Source, SourceDTO, CreateSourceRequest, PatchSourceRequest> {
    @Override
    SourceDTO toDto(PatchSourceRequest patchDto);

    @Override
    SourceDTO toDto(CreateSourceRequest createSourceRequest);
}