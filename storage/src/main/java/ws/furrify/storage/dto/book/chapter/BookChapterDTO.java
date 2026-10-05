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
package ws.furrify.storage.dto.book.chapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookChapterDTO extends UserScopedEntityDTO<BookChapter> {
    private String title;

    private String externalId;
    private Integer chapterNumber;

    @JsonIgnoreProperties("chapters")
    private BookDTO book;

    private Long views;
    private Long currentNumberOfWords;

    private List<SourceDTO> sources;
    @JsonIgnoreProperties("chapter")
    private List<BookChapterVersionDTO> versions;
    private Integer versionsCount;

    private ZonedDateTime publishDate;
}
