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
package ws.furrify.storage.dto.book.chapter.version.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;

import java.time.ZonedDateTime;

@Data
public class PatchBookChapterVersionRequest implements BasePatchEntityRequest<BookChapterVersion, BookChapterVersionDTO> {
    private JsonNullable<@NotNull String> contentHtml = JsonNullable.undefined();
    private JsonNullable<String> contentStylesheet = JsonNullable.undefined();

    private JsonNullable<String> authorNotesEnd = JsonNullable.undefined();
    private JsonNullable<String> authorNotesStart = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> chapter = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> contentUpdatedAt = JsonNullable.undefined();
}
