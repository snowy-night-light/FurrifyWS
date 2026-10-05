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
package ws.furrify.storage.domain.book.chapter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.domain.source.Source;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@EntityListeners(BookChapterEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookChapter extends UserScopedEntity {
    @NotBlank
    @Column(nullable = false, length = 255)
    String title;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "book_id")
    Book book;

    @Column()
    String externalId;

    @Column(nullable = false)
    Integer chapterNumber;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "chapter")
    List<BookChapterVersion> versions;

    @Builder.Default
    @Column(nullable = false)
    Integer versionsCount = 0;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    List<Source> sources;

    @Builder.Default
    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    Long views = 0L;

    @Builder.Default
    @Column(nullable = false)
    Long currentNumberOfWords = 0L;

    @Column(nullable = true)
    ZonedDateTime publishDate;
}