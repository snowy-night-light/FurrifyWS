/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.service.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.MediaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import static org.openapitools.model.FileUploadStatus.UPLOADED;


@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Value("${FURRIFY_CDN_URL:}")
    private String cdnUrl;

    public File downloadAttachment(UUID fileReferenceId, UUID ownerId, String tmpPrefix) {
        return downloadAttachment(fileReferenceId, ownerId, tmpPrefix, true);
    }

    public File downloadAttachment(UUID fileReferenceId, UUID ownerId, String tmpPrefix, boolean mockFeignContext) {
        if (ownerId != null && mockFeignContext) {
            SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
        }

        Path tempFilePath;

        try {
            AttachmentFileDTO attachmentFileDTO = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileReferenceId).getBody();

            if (attachmentFileDTO == null || attachmentFileDTO.getFileUri() == null || !UPLOADED.name().equals(Objects.requireNonNull(attachmentFileDTO.getUploadStatus()).name())) {
                log.error("File reference [id={}] not found or not uploaded! Cannot process scheduled task.", fileReferenceId);
                return null;
            }

            tempFilePath = Files.createTempFile(tmpPrefix, "." + attachmentFileDTO.getFileExtension());

            URI fileUri = attachmentFileDTO.getFileUri();
            String urlString = fileUri.toString();
            if (cdnUrl != null && !cdnUrl.isBlank() && !fileUri.isAbsolute()) {
                urlString = cdnUrl + (urlString.startsWith("/") ? "" : "/") + urlString;
            }
            try (InputStream in = URI.create(urlString).toURL().openStream()) {
                Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("Failed to fetch attachment file reference [id={}]: {}", fileReferenceId, e.getMessage());

            return null;
        } finally {
            if (mockFeignContext) {
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        }

        return tempFilePath.toFile();
    }

    public File downloadAttachment(MediaDTO mediaDTO, String tmpPrefix, boolean mockFeignContext) {
        if (mediaDTO == null || mediaDTO.getFileReferenceId() == null) {
            return null;
        }
        return downloadAttachment(mediaDTO.getFileReferenceId(), mediaDTO.getOwnerId(), tmpPrefix, mockFeignContext);
    }

    public File downloadAttachment(MediaDTO mediaDTO, String tmpPrefix) {
        if (mediaDTO == null || mediaDTO.getFileReferenceId() == null) {
            return null;
        }
        return downloadAttachment(mediaDTO.getFileReferenceId(), mediaDTO.getOwnerId(), tmpPrefix);
    }

    public boolean deleteAttachment(UUID fileReferenceId, UUID ownerId) {
        return deleteAttachment(fileReferenceId, ownerId, true);
    }

    public boolean deleteAttachment(UUID fileReferenceId, UUID ownerId, boolean mockFeignContext) {
        if (ownerId != null && mockFeignContext) {
            SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
        }

        try {
            attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(fileReferenceId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete attachment file reference [id={}]: {}", fileReferenceId, e.getMessage());
            return false;
        } finally {
            if (mockFeignContext) {
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        }
    }

    public boolean cleanupTmpFile(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }

        return false;
    }
}