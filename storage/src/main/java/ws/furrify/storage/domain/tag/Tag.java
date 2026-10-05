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
package ws.furrify.storage.domain.tag;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.library.Library;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag extends UserScopedEntity {
    @Column(unique = true, length = 64)
    @Size(max = 64)
    @Pattern(regexp = "^[ _-]*[a-z0-9]+(?:[ _-]+[a-z0-9]+)*[ _-]*$")
    @NotBlank
    String name;

    @OneToMany(mappedBy = "targetTag", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @ToString.Exclude
    List<TagAlias> aliases;

    @ManyToOne
    @NotNull
    TagCategory category;

    @ManyToOne(optional = true)
    @JoinColumn(name = "library_id")
    Library library;
}
