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
package ws.furrify.storage.domain.artist;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Artist extends UserScopedEntity {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "artist_nicknames",
            joinColumns = @JoinColumn(name = "artist_id")
    )
    @NotEmpty
    List<ArtistNickname> nicknames;

    @Column()
    String externalId;

    @Column(length = 2048)
    @Length(max = 2048)
    String bioHtml;

    @Builder.Default
    @Column(nullable = false)
    @PositiveOrZero
    Integer followersCount = 0;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @ToString.Exclude
    List<Source> sources;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    Media avatar;

    @ManyToOne(optional = true)
    @JoinColumn(name = "library_id")
    Library library;

    @Column(nullable = true)
    ZonedDateTime externalUpdatedAt;
}