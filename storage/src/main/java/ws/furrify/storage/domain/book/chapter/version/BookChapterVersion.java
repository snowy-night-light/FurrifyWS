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
package ws.furrify.storage.domain.book.chapter.version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.media.Media;

import java.time.ZonedDateTime;
import java.util.List;
import org.hibernate.Length;

@Entity
@Table(indexes = {
        @Index(name = "idx_book_chapter_version_chapter", columnList = "chapter_id, chapter_version"),
        @Index(name = "idx_book_chapter_version_media_missing", columnList = "media_missing")
})
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookChapterVersion extends UserScopedEntity {
    @Column(nullable = false)
    @Min(1)
    @NotNull
    Integer chapterVersion;

    @Column(nullable = true, length = Length.LONG32)
    String contentStylesheet;

    @Column(nullable = false, length = Length.LONG32)
    @NotNull
    String contentHtml;

    @Column(nullable = true, length = Length.LONG32)
    String authorNotesEnd;

    @Column(nullable = true, length = Length.LONG32)
    String authorNotesStart;

    @Builder.Default
    @Column(nullable = false)
    @NotNull
    Long wordCount = 0L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "chapter_id")
    BookChapter chapter;

    /**
     * Images used in this version's content (the {@code <img>} tags point at their files). Many
     * to many: the same image can appear in several versions/chapters. No cascade: deleting a
     * version never deletes media.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "book_chapter_version_media",
            joinColumns = @JoinColumn(name = "book_chapter_version_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id"),
            indexes = {
                    @Index(name = "idx_bcv_media_version", columnList = "book_chapter_version_id"),
                    @Index(name = "idx_bcv_media_media", columnList = "media_id")
            })
    @BatchSize(size = 50)
    @ToString.Exclude
    List<Media> media;

    @Builder.Default
    @Column(nullable = false)
    ZonedDateTime contentUpdatedAt = ZonedDateTime.now();

    /**
     * Some images in the content could not be stored yet (their tags still point at the original
     * host); importers retry them and clear this. Column default so existing rows get false.
     */
    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("false")
    Boolean mediaMissing = false;
}