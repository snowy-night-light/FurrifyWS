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
package ws.furrify.storage.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PatchBookRequest implements BasePatchEntityRequest<Book, BookDTO> {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> externalId = JsonNullable.undefined();

    private JsonNullable<@NotNull @Length(max = 40240) String> descriptionHtml = JsonNullable.undefined();
    private JsonNullable<@NotNull @Length(max = 10240) String> shortDescriptionHtml = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> cover = JsonNullable.undefined();
    private JsonNullable<List<EntityIdRequest>> sequels = JsonNullable.undefined();
    private JsonNullable<List<EntityIdRequest>> prequels = JsonNullable.undefined();
    private JsonNullable<@NotNull BookStatus> status = JsonNullable.undefined();
    private JsonNullable<@NotNull BookRating> rating = JsonNullable.undefined();
    private JsonNullable<@NotNull EntityIdRequest> library = JsonNullable.undefined();
    private JsonNullable<@NotNull @PositiveOrZero Integer> likes = JsonNullable.undefined();
    private JsonNullable<@NotNull @PositiveOrZero Integer> dislikes = JsonNullable.undefined();
    private JsonNullable<@NotNull Long> views = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> tags = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> artists = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> publishDate = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> externalUpdatedAt = JsonNullable.undefined();
}
