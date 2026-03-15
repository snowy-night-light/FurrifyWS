package ws.furrify.storage.dto.source;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SourceDTO extends UserScopedEntityDTO<Source> {
    private SourceStrategy strategy;
}
