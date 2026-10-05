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
package ws.furrify.storage.dto.book.chapter.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PatchBookChapterRequest implements BasePatchEntityRequest<BookChapter, BookChapterDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> externalId = JsonNullable.undefined();
    private JsonNullable<@NotNull @Positive Integer> chapterNumber = JsonNullable.undefined();
    private JsonNullable<@NotNull @PositiveOrZero Long> views = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> book = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> publishDate = JsonNullable.undefined();
}
