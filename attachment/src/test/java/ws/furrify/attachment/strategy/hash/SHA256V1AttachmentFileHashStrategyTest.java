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
package ws.furrify.attachment.strategy.hash;

import org.junit.jupiter.api.Test;
import ws.furrify.attachment.domain.file.strategy.hash.SHA256V1AttachmentFileHashStrategy;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SHA256V1AttachmentFileHashStrategyTest extends AttachmentFileHashStrategyTest {
    public SHA256V1AttachmentFileHashStrategyTest() {
        super(new SHA256V1AttachmentFileHashStrategy());
    }

    private final String VALID_HASH = "aad96d410d92b5589d41e8462507e3af57682022db3d3711a236c0245fcf296e";

    @Test
    @Override
    void testHash() {
        String hash = getHashForExampleFile();

        assertEquals(VALID_HASH, hash);
    }

    @Test
    @Override
    void validateHash() {
        assertTrue(validateHashForExampleFile(VALID_HASH));
    }

    @Test
    @Override
    void validateHashType() {
        assertEquals(AttachmentFileHashType.SHA256, strategy.getHashType());
    }
}
