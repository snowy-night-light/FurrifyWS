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
package ws.furrify.attachment.dto.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.dto.file.vo.AttachmentFileHashDTO;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(name = "AttachmentFileDTO")
public class AttachmentFileDTO extends UserScopedEntityDTO<AttachmentFile> {
    private String fileName;
    private String fileExtension;

    private List<AttachmentFileHashDTO> fileHashes;

    private String mimeType;

    private FileUploadStatus uploadStatus;

    private Long fileSize;

    private URI fileUri;
    private URI thumbnailUri;

    private String storageServiceId;
}
