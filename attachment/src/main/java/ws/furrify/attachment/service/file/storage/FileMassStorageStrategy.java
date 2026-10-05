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
import java.util.UUID;

public interface FileMassStorageStrategy extends StrategyIntf {
    UploadedFileReference uploadFile(UUID id, String mimeType, File file, boolean replaceExisting);

    UploadedFileReference linkFile(UUID id, String mimeType, URI existingFileUri, URI existingThumbnailUri);

    boolean removeFileDirectory(UUID id);

    String getStorageServiceId();
}
