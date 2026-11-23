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
package ws.furrify.storage.domain.collection;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.post.Post;

import java.util.List;

@Entity
@Table(indexes = {
        @Index(name = "idx_collection_owner_created", columnList = "owner_id, created_at"),
        @Index(name = "idx_collection_library", columnList = "library_id")
})
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Collection extends UserScopedEntity {
    @Column(nullable = true, length = 128)
    @Size(max = 128)
    String title;

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinTable(name = "collection_posts", joinColumns = @JoinColumn(name = "collection_id"), inverseJoinColumns = @JoinColumn(name = "posts_id"),
            indexes = {
                    @Index(name = "idx_collection_posts_collection", columnList = "collection_id"),
                    // Post.collections (mappedBy) loads by this column
                    @Index(name = "idx_collection_posts_post", columnList = "posts_id")
            })
    List<Post> posts;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;
}