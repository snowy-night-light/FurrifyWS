package ws.furrify.storage.domain.source;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.converters.StrategyDBConverter;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Source extends BaseEntity {
    @Convert(converter = StrategyDBConverter.class)
    SourceStrategy strategy;
}