/*
 * furrify-attachment-service - Furrify Workspace Project
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
package ws.furrify.attachment.dto.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.core.entity.request.BasePatchEntityRequest;

@Data
public class PatchAttachmentFileRequest implements BasePatchEntityRequest<AttachmentFile, AttachmentFileDTO> {

    private JsonNullable<@NotBlank String> fileName = JsonNullable.undefined();

    private JsonNullable<@NotNull MultipartFile> file = JsonNullable.undefined();
}
