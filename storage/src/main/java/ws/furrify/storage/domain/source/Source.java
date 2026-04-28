package ws.furrify.storage.domain.source;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.converters.StrategyDBConverter;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Source extends UserScopedEntity {
    @Convert(converter = StrategyDBConverter.class)
    @Column(nullable = false)
    @NotNull
    SourceStrategy strategy;
}