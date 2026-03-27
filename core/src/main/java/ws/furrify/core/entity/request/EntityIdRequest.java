package ws.furrify.core.entity.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EntityIdRequest {
    private UUID id;
}
