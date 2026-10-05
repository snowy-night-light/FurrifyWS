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
package ws.furrify.storage.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;

import java.util.List;

@Data
public class PatchPostRequest implements BasePatchEntityRequest<Post, PostDTO> {

    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> description = JsonNullable.undefined();

    private JsonNullable<@NotEmpty List<@NotNull EntityIdRequest>> tags = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> artists = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> displayMediaList = JsonNullable.undefined();
    private JsonNullable<List<@NotNull EntityIdRequest>> attachments = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();

    private JsonNullable<@NotNull EntityIdRequest> library = JsonNullable.undefined();
}
