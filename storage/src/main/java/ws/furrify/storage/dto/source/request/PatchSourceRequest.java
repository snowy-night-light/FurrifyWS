package ws.furrify.storage.dto.source.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;
import ws.furrify.storage.dto.source.SourceDTO;

import java.util.List;
import java.util.Map;

@Data
public class PatchSourceRequest implements BasePatchEntityRequest<Source, SourceDTO> {
    private JsonNullable<Map<String, Object>> data = JsonNullable.undefined();

    private JsonNullable<@NotNull SourceStrategy> sourceStrategy = JsonNullable.undefined();
}
