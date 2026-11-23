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
import org.hibernate.annotations.BatchSize;
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
@Table(indexes = {
        @Index(name = "idx_book_library_external_id", columnList = "library_id, external_id"),
        // owner_id: user-scope filter on every query; created_at: per-day counts on the statistics page
        @Index(name = "idx_book_owner_created", columnList = "owner_id, created_at"),
        // file-generation sweeps (every 60 s) over the whole table
        @Index(name = "idx_book_needs_generation", columnList = "needs_book_file_generation"),
        @Index(name = "idx_book_active_worker_task", columnList = "active_worker_task_id")
})
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

    @Column(columnDefinition = "TEXT", nullable = false, length = 92160)
    @NotNull
    @Length(max = 92160)
    String descriptionHtml;

    @Column()
    String externalId;

    @Column(columnDefinition = "TEXT", nullable = false, length = 46080)
    @NotNull
    @Length(max = 46080)
    String shortDescriptionHtml;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_sequels", joinColumns = @JoinColumn(name = "book_id"),
            indexes = @Index(name = "idx_book_sequels_book", columnList = "book_id"))
    @BatchSize(size = 50)
    List<UUID> sequels;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_prequels", joinColumns = @JoinColumn(name = "book_id"),
            indexes = @Index(name = "idx_book_prequels_book", columnList = "book_id"))
    @BatchSize(size = 50)
    List<UUID> prequels;

    /** Not cascaded: unused media are removed by {@code MediaOrphanCleanupService}. */
    @OneToOne(fetch = FetchType.EAGER)
    Media cover;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "book_tags", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "tags_id"),
            indexes = {
                    @Index(name = "idx_book_tags_book", columnList = "book_id"),
                    @Index(name = "idx_book_tags_tag", columnList = "tags_id")
            })
    @BatchSize(size = 50)
    List<Tag> tags;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "book_artists", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "artists_id"),
            indexes = {
                    @Index(name = "idx_book_artists_book", columnList = "book_id"),
                    @Index(name = "idx_book_artists_artist", columnList = "artists_id")
            })
    @BatchSize(size = 50)
    List<Artist> artists;

    @Enumerated(EnumType.STRING)
    BookStatus status;

    @Enumerated(EnumType.STRING)
    BookRating rating;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "book")
    List<BookChapter> chapters;

    /**
     * Maintained by {@code BookRepository.refreshChaptersCount} (SQL), never by entity saves:
     * {@code updatable = false} stops a merge of a stale DTO from overwriting it.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    Integer chaptersCount = 0;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "book_sources", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "sources_id"),
            indexes = @Index(name = "idx_book_sources_book", columnList = "book_id"))
    @BatchSize(size = 50)
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
            joinColumns = {@JoinColumn(name = "book_id", referencedColumnName = "id")},
            indexes = @Index(name = "idx_book_format_id_map_book", columnList = "book_id"))
    @MapKeyColumn(name = "format")
    @BatchSize(size = 50)
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