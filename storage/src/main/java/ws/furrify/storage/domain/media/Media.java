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
package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.Min;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.Source;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(MediaEntityListener.class)
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media extends UserScopedEntity {

    @Column(nullable = false)
    @Min(0)
    @NotNull
    Integer priority;

    @Column(nullable = false)
    @NotNull
    UUID fileReferenceId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<Source> sources;

    @Column(nullable = true)
    ZonedDateTime externalUpdatedAt;
}