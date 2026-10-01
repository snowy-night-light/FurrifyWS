package ws.furrify.core.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserScopedEntityDTO<ENTITY extends UserScopedEntity> extends BaseEntityDTO<ENTITY> {
    private UUID ownerId;
}
