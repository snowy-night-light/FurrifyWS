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
package ws.furrify.attachment.service.file.storage;

import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.model.StrategyIntf;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

public interface FileMassStorageStrategy extends StrategyIntf {
    UploadedFileReference uploadFile(UUID id, String mimeType, File file, boolean replaceExisting);

    /**
     * Store {@code file} (e.g. a JPEG XL conversion) with a thumbnail made from
     * {@code thumbnailSource} (the uploaded original, which the thumbnail generator can read).
     */
    default UploadedFileReference uploadFile(UUID id, File file, File thumbnailSource, String thumbnailSourceMimeType, boolean replaceExisting) {
        return uploadFile(id, thumbnailSourceMimeType, file, replaceExisting);
    }

    /** Absolute path of a stored file's URI (e.g. {@code /{id}/attachment.jxl}). */
    Path resolve(URI fileUri);

    /** Store a JPEG thumbnail for {@code id} (as JPEG XL when it converts); its URI. */
    URI storeThumbnailJpeg(UUID id, File thumbnailJpeg) throws java.io.IOException;

    UploadedFileReference linkFile(UUID id, String mimeType, URI existingFileUri, URI existingThumbnailUri);

    boolean removeFileDirectory(UUID id);

    String getStorageServiceId();
}
