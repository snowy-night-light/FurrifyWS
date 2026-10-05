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
package ws.furrify.attachment.domain.file.strategy.hash;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.springframework.stereotype.Component;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;

import java.io.File;
import java.io.IOException;

@Component
public class SHA256V1AttachmentFileHashStrategy implements AttachmentFileHashStrategy {
    @Override
    public String calculateHash(File file) {
        try {
            return Files.asByteSource(file).hash(Hashing.sha256()).toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean validateHash(String hash, File file) {
        return hash.equals(calculateHash(file));
    }

    @Override
    public AttachmentFileHashType getHashType() {
        return AttachmentFileHashType.SHA256;
    }
}
