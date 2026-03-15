package ws.furrify.core.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseEntityDTO<ENTITY extends BaseEntity> {
    private UUID id;

    private long version;

    private String modifiedBy;
    private ZonedDateTime modifiedAt;

    private String createdBy;
    private ZonedDateTime createdAt;
}
