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
package ws.furrify.attachment.service.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttachmentFileExtensionTest {

    @Test
    void extensionComesFromTheNameWhenItHasOne() {
        assertEquals("png", AttachmentFileProcessorService.fileExtension("cover.png", "image/png"));
        assertEquals("jpeg", AttachmentFileProcessorService.fileExtension("photo.jpeg", "image/jpeg"));
        assertEquals("md", AttachmentFileProcessorService.fileExtension("notes.md", "text/plain"), "only images follow their content");
        assertEquals("zip", AttachmentFileProcessorService.fileExtension("fimfarchive-20260915.zip", "application/zip"));
    }

    @Test
    void imageContentWinsOverAWrongExtension() {
        assertEquals("gif", AttachmentFileProcessorService.fileExtension("cover-56862.jpg", "image/gif"));
        assertEquals("png", AttachmentFileProcessorService.fileExtension("avatar-188042.jpg", "image/png"));
    }

    @Test
    void extensionComesFromTheTypeWhenTheNameHasNone() {
        assertEquals("png", AttachmentFileProcessorService.fileExtension("image", "image/png"));
        assertEquals("jpg", AttachmentFileProcessorService.fileExtension("photo.", "image/jpeg"));
        assertEquals("gif", AttachmentFileProcessorService.fileExtension("view.php?id=1", "image/gif"));
        assertEquals("bin", AttachmentFileProcessorService.fileExtension("blob", "application/x-unknown-thing"));
        assertEquals("bin", AttachmentFileProcessorService.fileExtension(null, null));
    }
}
