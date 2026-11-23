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
package ws.furrify.storage.service.book;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookFileCleanupServiceTest {

    @Test
    void onlyWorkerGeneratedBookFilesAreCandidates() {
        Pattern generated = BookFileCleanupService.generatedFileName(List.of("epub", "html"));

        assertTrue(generated.matcher("3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab.epub").matches());
        assertTrue(generated.matcher("3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab.html").matches());
        assertFalse(generated.matcher("my story.epub").matches());
        assertFalse(generated.matcher("cover-12345.jpg").matches());
        assertFalse(generated.matcher("3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab.zip").matches(), "not an extension of a generator");
    }

    @Test
    void aNewGeneratorsExtensionIsRecognisedToo() {
        Pattern generated = BookFileCleanupService.generatedFileName(List.of("epub", "pdf"));

        assertTrue(generated.matcher("3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab.pdf").matches());
        assertFalse(generated.matcher("3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab.html").matches());
    }
}
