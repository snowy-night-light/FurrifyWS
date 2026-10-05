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
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.category.TagCategory;

import java.util.List;

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
}