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
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateArtistRequest implements BaseCreateEntityRequest<Artist, ArtistDTO> {

    private String externalId;

    @NotEmpty
    private List<@NotNull ArtistNickname> nicknames;

    private List<@NotNull EntityIdRequest> sources;

    @NotNull
    @PositiveOrZero
    private Integer followersCount;

    @Length(min = 0, max = 2048)
    private String bioHtml;
    private EntityIdRequest avatar;

    private EntityIdRequest library;

    private ZonedDateTime externalUpdatedAt;
}
