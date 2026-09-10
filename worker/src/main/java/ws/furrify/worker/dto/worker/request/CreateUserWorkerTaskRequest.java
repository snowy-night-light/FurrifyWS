package ws.furrify.worker.dto.worker.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;

import java.time.ZonedDateTime;

@Data
public class CreateUserWorkerTaskRequest<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> implements BaseCreateEntityRequest<ENTITY, DTO> {

    @NotNull
    private ZonedDateTime startAt;
}
