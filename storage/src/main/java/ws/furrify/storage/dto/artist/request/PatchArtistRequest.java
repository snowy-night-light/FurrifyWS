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
package ws.furrify.storage.dto.artist.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PatchArtistRequest implements BasePatchEntityRequest<Artist, ArtistDTO> {

    private JsonNullable<@NotEmpty List<@NotNull ArtistNickname>> nicknames = JsonNullable.undefined();

    private JsonNullable<String> externalId = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();

    private JsonNullable<@Length(min = 0, max = 2048) String> bioHtml = JsonNullable.undefined();
    private JsonNullable<@NotNull @PositiveOrZero Integer> followersCount = JsonNullable.undefined();
    private JsonNullable<EntityIdRequest> avatar = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> library = JsonNullable.undefined();
    private JsonNullable<ZonedDateTime> externalUpdatedAt = JsonNullable.undefined();
}
