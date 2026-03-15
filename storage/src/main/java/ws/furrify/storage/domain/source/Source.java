package ws.furrify.storage.domain.source;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.converters.StrategyDBConverter;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Source extends UserScopedEntity {
    @Convert(converter = StrategyDBConverter.class)
    SourceStrategy strategy;
}