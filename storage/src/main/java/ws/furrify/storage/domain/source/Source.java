/*
 * furrify-storage-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.storage.domain.source;

import jakarta.persistence.Column;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
// strategy: lets specs like "sources.strategy = 'X' && sources.dataUrl = '...'" narrow to one strategy's sources
@Table(indexes = {
        @Index(name = "idx_source_strategy", columnList = "strategy"),
        @Index(name = "idx_source_data_url", columnList = "data_url")
})
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

    /**
     * Copy of {@code data.url}, kept in sync on every save, so sources can be looked up by URL
     * with an index ({@code dataUrl = '...'}); a filter on {@code data.url} reads the JSON of every
     * row. A plain column rather than a database-generated one, so it works on every database.
     */
    @Column(name = "data_url", columnDefinition = "TEXT")
    private String dataUrl;

    @PrePersist
    @PreUpdate
    void syncDataUrl() {
        Object url = data == null ? null : data.get("url");
        dataUrl = url == null ? null : url.toString();
    }

    @Convert(converter = StrategyDBConverter.class)
    @Column(nullable = false)
    @NotNull
    private SourceStrategy strategy;
}