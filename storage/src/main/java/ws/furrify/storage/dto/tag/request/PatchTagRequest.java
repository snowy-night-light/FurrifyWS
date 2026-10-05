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
package ws.furrify.storage.dto.tag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

@Data
public class PatchTagRequest implements BasePatchEntityRequest<Tag, TagDTO> {

    private JsonNullable<EntityIdRequest> category = JsonNullable.undefined();

    private JsonNullable<@NotBlank @Pattern(regexp = "^[ _-]*[a-z0-9]+(?:[ _-]+[a-z0-9]+)*[ _-]*$") String> name = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> library = JsonNullable.undefined();
}
