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
package ws.furrify.storage.domain.media;

import jakarta.persistence.PostRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;

import java.util.UUID;

/**
 * Removes a deleted media's file from the attachment service, after commit and only when no other
 * media still uses it (importers share one file between media, deduplicated by hash).
 */
@Slf4j
@Component
class MediaEntityListener {

    private final AttachmentFileV1RestControllerApiClient attachmentClient;
    private final MediaRepository mediaRepository;
    private final AsyncUtils asyncUtils;

    @Autowired
    MediaEntityListener(@Lazy AttachmentFileV1RestControllerApiClient attachmentClient,
                        @Lazy MediaRepository mediaRepository,
                        @Lazy AsyncUtils asyncUtils) {
        this.attachmentClient = attachmentClient;
        this.mediaRepository = mediaRepository;
        this.asyncUtils = asyncUtils;
    }

    @PostRemove
    public void postRemove(Media media) {
        UUID fileId = media.getFileReferenceId();
        UUID ownerId = media.getOwnerId();
        if (fileId == null) return;
        asyncUtils.runAfterCommitNoTransaction(() -> {
            try {
                if (mediaRepository.existsByFileReferenceId(fileId)) return;
                boolean mocked = ownerId != null && SecurityContextUtils.getCurrentUserPrincipal().isEmpty();
                if (mocked) SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
                try {
                    attachmentClient.attachmentFileV1RestControllerDelete(fileId);
                } finally {
                    if (mocked) SecurityContextUtils.clearFeignClientSecurityContext();
                }
            } catch (RuntimeException e) {
                log.warn("Could not delete attachment {} of removed media {}: {}", fileId, media.getId(), e.getMessage());
            }
        });
    }
}
