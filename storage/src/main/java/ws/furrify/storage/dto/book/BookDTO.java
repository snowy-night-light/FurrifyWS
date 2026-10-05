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
package ws.furrify.storage.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.tag.TagDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookDTO extends UserScopedEntityDTO<Book> {
    private String title;

    private String externalId;

    private String descriptionHtml;
    private String shortDescriptionHtml;

    private MediaDTO cover;
    private List<UUID> prequels;
    private List<UUID> sequels;

    private Long totalWordCount;

    private Long views;
    @Nullable
    private Integer likes;
    @Nullable
    private Integer dislikes;

    private BookStatus status;
    private BookRating rating;

    @JsonIgnoreProperties("book")
    private List<BookChapterDTO> chapters;
    private Integer chaptersCount;

    private List<TagDTO> tags;
    private List<ArtistDTO> artists;
    private List<SourceDTO> sources;

    private LibraryDTO library;

    private Map<String, UUID> formatReferenceIds;
    private UUID activeWorkerTaskId;


    private ZonedDateTime publishDate;
    private ZonedDateTime externalUpdatedAt;
    @Builder.Default
    private Boolean needsBookFileGeneration = false;
    @Builder.Default
    private Integer generationRetryCount = 0;
    @Builder.Default
    private Boolean fileGenerationFailed = false;
}
