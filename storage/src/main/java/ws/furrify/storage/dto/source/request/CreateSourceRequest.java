package ws.furrify.storage.dto.source.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;
import ws.furrify.storage.dto.source.SourceDTO;

import java.util.Map;

@Data
public class CreateSourceRequest implements BaseCreateEntityRequest<Source, SourceDTO> {

    private Map<String, Object> data;

    @NotNull
    private SourceStrategy strategy;
}
