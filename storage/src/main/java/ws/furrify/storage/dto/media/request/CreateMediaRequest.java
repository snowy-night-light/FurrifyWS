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
package ws.furrify.storage.dto.media.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Min;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMediaRequest implements BaseCreateEntityRequest<Media, MediaDTO> {
    @NotNull
    @Min(0)
    private Integer priority;

    @NotNull
    private UUID fileReferenceId;

    private List<@NotNull EntityIdRequest> sources;

    private ZonedDateTime externalUpdatedAt;
}
