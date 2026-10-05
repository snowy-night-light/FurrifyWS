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
package ws.furrify.storage.dto.tag.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;

@Data
public class PatchTagCategoryRequest implements BasePatchEntityRequest<TagCategory, TagCategoryDTO> {
    JsonNullable<@NotBlank String> name = JsonNullable.undefined();

    JsonNullable<@Pattern(regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$") @NotBlank @Size(max = 7) String> hexColor = JsonNullable.undefined();

    JsonNullable<EntityIdRequest> library = JsonNullable.undefined();
}
