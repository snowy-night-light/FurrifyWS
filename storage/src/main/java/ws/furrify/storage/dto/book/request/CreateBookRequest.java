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

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.dto.book.BookDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateBookRequest implements BaseCreateEntityRequest<Book, BookDTO> {

    private String externalId;

    @NotBlank
    private String title;

    @Length(max = 40240)
    @NotNull
    private String descriptionHtml;

    @Length(max = 10240)
    @NotNull
    private String shortDescriptionHtml;

    private EntityIdRequest cover;
    private List<EntityIdRequest> sequels;
    private List<EntityIdRequest> prequels;
    @NotNull
    private BookStatus status;
    @NotNull
    private BookRating rating;
    @NotNull
    private Long views;
    @PositiveOrZero
    @Nullable
    private Integer likes;
    @PositiveOrZero
    @Nullable
    private Integer dislikes;
    @NotNull
    private EntityIdRequest library;
    private List<@NotNull EntityIdRequest> tags;
    private List<@NotNull EntityIdRequest> artists;
    private List<@NotNull EntityIdRequest> sources;

    private ZonedDateTime publishDate;
    private ZonedDateTime externalUpdatedAt;
}
