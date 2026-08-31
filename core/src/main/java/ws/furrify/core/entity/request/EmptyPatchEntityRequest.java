package ws.furrify.core.entity.request;

import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;

public interface EmptyPatchEntityRequest<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> extends BasePatchEntityRequest<ENTITY, DTO>{
}
