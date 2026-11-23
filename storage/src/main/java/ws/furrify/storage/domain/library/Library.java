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
package ws.furrify.storage.domain.library;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.category.TagCategory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Library extends UserScopedEntity {
    @Column(nullable = false, length = 128)
    @Size(max = 128)
    @NotBlank
    String title;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Post> posts;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Tag> tags;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<TagCategory> tagCategories;


    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Artist> artists;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Collection> collections;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Book> books;

    @Builder.Default
    @Column(nullable = false)
    Boolean likesEnabled = false;

    @Builder.Default
    @Column(nullable = false)
    Boolean dislikesEnabled = false;

    @Builder.Default
    @ColumnDefault("false")
    @Column(nullable = false)
    boolean scheduledForRemoval = false;

    /**
     * The content being removed from the library in the background (e.g. ["BOOKS", "TAGS"]), null when none:
     * a second clear is refused meanwhile, and a clear cut short by a restart is resumed. Only ever set by
     * {@code LibraryContentRemovalService}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    List<String> contentRemoval;

    /**
     * Lease of the running removal: refreshed every minute by the storage instance doing it. A removal whose
     * lease is older than a few minutes (its instance stopped or it failed) is taken over by any instance.
     * Internal: not in the DTO.
     */
    ZonedDateTime removalHeartbeat;

    /** {@code LAUNCH_ID} of the storage instance holding the removal lease. Internal: not in the DTO. */
    UUID removalOwner;

    /** Whether the library or some of its content is being removed (for clients). */
    public boolean isRemovalInProgress() {
        return scheduledForRemoval || (contentRemoval != null && !contentRemoval.isEmpty());
    }
}