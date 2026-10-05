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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.book.chapter.BookChapter;

import java.time.ZonedDateTime;

@Entity
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

    @Column(columnDefinition = "TEXT", nullable = true)
    String contentStylesheet;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    String contentHtml;

    @Column(columnDefinition = "TEXT", nullable = true)
    String authorNotesEnd;

    @Column(columnDefinition = "TEXT", nullable = true)
    String authorNotesStart;

    @Builder.Default
    @Column(nullable = false)
    @NotNull
    Long wordCount = 0L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "chapter_id")
    BookChapter chapter;

    @Builder.Default
    @Column(nullable = false)
    ZonedDateTime contentUpdatedAt = ZonedDateTime.now();
}