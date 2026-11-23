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
package ws.furrify.storage.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentHtmlSanitizerPictureTest {

    private static final String ID = "3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab";

    @Test
    void jxlPictureWithFallbackSurvives() {
        String html = "<picture><source type=\"image/jxl\" srcset=\"/" + ID + "/attachment.jxl\" />"
                + "<img src=\"/attachments/v1/files/" + ID + "/fallback\" alt=\"Art\" /></picture>";
        String out = ContentHtmlSanitizerUtil.sanitize(html);
        assertTrue(out.contains("<picture>"), out);
        assertTrue(out.contains("type=\"image/jxl\""), out);
        assertTrue(out.contains("srcset=\"/" + ID + "/attachment.jxl\""), out);
        assertTrue(out.contains("src=\"/attachments/v1/files/" + ID + "/fallback\""), out);
    }

    @Test
    void otherSourcesAreStripped() {
        String out = ContentHtmlSanitizerUtil.sanitize("<picture><source type=\"text/html\" srcset=\"javascript:alert(1)\" />"
                + "<source type=\"image/jxl\" srcset=\"//evil.example/x.jxl\" /><img src=\"/a/b.png\" /></picture>");
        assertFalse(out.contains("javascript"), out);
        assertFalse(out.contains("text/html"), out);
        assertFalse(out.contains("//evil.example"), out);
    }
}
