package ws.furrify.worker.dto.worker.request;

import jakarta.validation.constraints.NotNull;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.BasePatchEntityRequest;

import java.time.ZonedDateTime;

public class PatchUserWorkerTaskRequest<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> implements BasePatchEntityRequest<ENTITY, DTO> {

    @NotNull
    private JsonNullable<ZonedDateTime> startAt;
}
