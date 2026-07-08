package ws.furrify.storage.domain.source;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ws.furrify.core.converters.StrategyDBConverter;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

import java.util.Map;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Source extends UserScopedEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = true)
    private Map<String, Object> data;

    @Convert(converter = StrategyDBConverter.class)
    @Column(nullable = false)
    @NotNull
    private SourceStrategy strategy;
}