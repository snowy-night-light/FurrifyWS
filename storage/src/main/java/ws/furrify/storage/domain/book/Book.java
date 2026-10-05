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
package ws.furrify.storage.domain.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.tag.Tag;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@EntityListeners(BookEntityListener.class)
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book extends UserScopedEntity {
    @NotBlank
    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false, length = 10240)
    @NotNull
    @Length(max = 40240)
    String descriptionHtml;

    @Column()
    String externalId;

    @Column(columnDefinition = "TEXT", nullable = false, length = 1024)
    @NotNull
    @Length(max = 1024)
    String shortDescriptionHtml;

    @ElementCollection(fetch = FetchType.EAGER)
    List<UUID> sequels;

    @ElementCollection(fetch = FetchType.EAGER)
    List<UUID> prequels;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Media cover;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Tag> tags;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Artist> artists;

    @Enumerated(EnumType.STRING)
    BookStatus status;

    @Enumerated(EnumType.STRING)
    BookRating rating;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "book")
    List<BookChapter> chapters;

    @Column(nullable = false)
    @Builder.Default
    Integer chaptersCount = 0;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    List<Source> sources;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;

    @Column(nullable = false)
    @Builder.Default
    Long views = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long totalWordCount = 0L;

    @Column(nullable = true)
    Integer likes;

    @Column(nullable = true)
    Integer dislikes;

    @Column(nullable = true)
    ZonedDateTime publishDate;

    @Column(nullable = true)
    ZonedDateTime externalUpdatedAt;

    @Column(nullable = true)
    UUID activeWorkerTaskId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_format_id_map",
            joinColumns = {@JoinColumn(name = "book_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "format")
    Map<String, UUID> formatReferenceIds = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    Boolean needsBookFileGeneration = false;

    @Column(nullable = false)
    @Builder.Default
    Integer generationRetryCount = 0;

    @Column(nullable = false)
    @Builder.Default
    Boolean fileGenerationFailed = false;
}