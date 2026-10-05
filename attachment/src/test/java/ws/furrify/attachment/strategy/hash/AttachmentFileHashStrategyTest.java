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

import lombok.SneakyThrows;
import ws.furrify.attachment.domain.file.strategy.hash.AttachmentFileHashStrategy;

import java.io.File;
import java.net.URL;

public abstract class AttachmentFileHashStrategyTest {

    protected final AttachmentFileHashStrategy strategy;

    public AttachmentFileHashStrategyTest(AttachmentFileHashStrategy strategy) {
        this.strategy = strategy;
    }

    @SneakyThrows
    private File getExampleFile() {
        URL resourceUrl = getClass().getClassLoader().getResource("files/image/example.png");
        assert resourceUrl != null;
        return new File(resourceUrl.toURI());
    }

    protected String getHashForExampleFile() {


        return strategy.calculateHash(getExampleFile());
    }

    protected boolean validateHashForExampleFile(String hash) {
        return strategy.validateHash(hash, getExampleFile());
    }

    abstract void testHash();
    abstract void validateHash();
    abstract void validateHashType();
}
