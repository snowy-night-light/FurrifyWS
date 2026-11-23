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
package ws.furrify.storage.domain.post;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.tag.Tag;

import java.util.List;

@Entity
@Table(indexes = {
        @Index(name = "idx_post_owner_created", columnList = "owner_id, created_at"),
        @Index(name = "idx_post_library", columnList = "library_id")
})
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends UserScopedEntity {
    @Column(nullable = false, length = 128)
    @Size(max = 128)
    @NotBlank
    String title;
    @Column(length = 10240)
    @Size(max = 10240)
    String description;

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tags_id"),
            indexes = {
                    @Index(name = "idx_post_tags_post", columnList = "post_id"),
                    @Index(name = "idx_post_tags_tag", columnList = "tags_id")
            })
    @NotEmpty
    List<Tag> tags;
    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinTable(name = "post_artists", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "artists_id"),
            indexes = {
                    @Index(name = "idx_post_artists_post", columnList = "post_id"),
                    @Index(name = "idx_post_artists_artist", columnList = "artists_id")
            })
    List<Artist> artists;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "post_display_media_list", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "display_media_list_id"),
            indexes = {
                    @Index(name = "idx_post_display_media_post", columnList = "post_id"),
                    @Index(name = "idx_post_display_media_media", columnList = "display_media_list_id")
            })
    List<Media> displayMediaList;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_attachments", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "attachments_id"),
            indexes = {
                    @Index(name = "idx_post_attachments_post", columnList = "post_id"),
                    @Index(name = "idx_post_attachments_media", columnList = "attachments_id")
            })
    List<Media> attachments;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "post_sources", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "sources_id"),
            indexes = @Index(name = "idx_post_sources_post", columnList = "post_id"))
    List<Source> sources;

    @ManyToMany(mappedBy = "posts", fetch = FetchType.LAZY)
    List<Collection> collections;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;
}