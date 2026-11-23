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
package ws.furrify.worker.generator;

import io.documentnode.epub4j.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Content stores JPEG XL images as {@code <picture>} with CDN-relative srcset and a gateway-relative
 * fallback: HTML files link both absolutely, EPUBs pack the fallback (original format) as a file.
 */
class EPubGeneratorBookFileImageTest {

    private static final String CDN = "https://cdn.furrify.test";
    private static final String GATEWAY = "https://api.furrify.test";
    private static final String ID = "3f2a6c1e-8a4b-4c1d-9e7f-0123456789ab";
    private static final String PICTURE = "<picture><source type=\"image/jxl\" srcset=\"/" + ID + "/attachment.jxl\" />"
            + "<img src=\"/attachments/v1/files/" + ID + "/fallback\" alt=\"Art\" /></picture>";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0};

    /** EPUB generator whose downloads are answered from memory (and recorded). */
    private static EPubGeneratorBookFile epub(List<String> requested) {
        EPubGeneratorBookFile g = new EPubGeneratorBookFile(null) {
            @Override
            Fetched fetchImage(String url, BookFileUserWorkerTaskDTO task) {
                requested.add(url);
                return url.contains("missing") ? null : new Fetched(PNG, "image/png", "png");
            }
        };
        ReflectionTestUtils.setField(g, "cdnUrl", CDN + "/");
        ReflectionTestUtils.setField(g, "gatewayUrl", GATEWAY);
        return g;
    }

    private static HtmlGeneratorBookFile html() {
        HtmlGeneratorBookFile g = new HtmlGeneratorBookFile(null);
        ReflectionTestUtils.setField(g, "cdnUrl", CDN);
        ReflectionTestUtils.setField(g, "gatewayUrl", GATEWAY + "/");
        return g;
    }

    @Test
    void htmlLinksJxlFromTheCdnAndTheFallbackThroughTheGateway() {
        String out = html().withCdnUrls(PICTURE + "<img src=\"https://example.com/b.png\" />");
        assertEquals("<picture><source type=\"image/jxl\" srcset=\"" + CDN + "/" + ID + "/attachment.jxl\" />"
                + "<img src=\"" + GATEWAY + "/attachments/v1/files/" + ID + "/fallback\" alt=\"Art\" /></picture>"
                + "<img src=\"https://example.com/b.png\" />", out);
    }

    @Test
    void epubPacksTheFallbackOncePerBook() {
        List<String> requested = new ArrayList<>();
        EPubGeneratorBookFile g = epub(requested);
        Book book = new Book();
        Map<String, String> packed = new HashMap<>();

        String first = g.packImages("<p>a</p>" + PICTURE, packed, book, null);
        String second = g.packImages(PICTURE, packed, book, null);

        assertTrue(first.matches("<p>a</p><img src=\"images/[0-9a-f]{20}\\.png\" alt=\"Art\" />"), first);
        assertEquals(second, first.substring("<p>a</p>".length()));
        assertEquals(List.of(GATEWAY + "/attachments/v1/files/" + ID + "/fallback"), requested, "downloaded once");
        assertNotNull(book.getResources().getByHref(first.replaceAll(".*src=\"([^\"]+)\".*", "$1")));
    }

    @Test
    void epubPacksPlainCdnImagesAndLeavesOtherHostsAlone() {
        List<String> requested = new ArrayList<>();
        String out = epub(requested).packImages("<img src=\"/" + ID + "/attachment.png\" alt=\"x\" /><img src=\"https://example.com/b.png\" />",
                new HashMap<>(), new Book(), null);
        assertTrue(out.matches("<img src=\"images/[0-9a-f]{20}\\.png\" alt=\"x\" /><img src=\"https://example.com/b.png\" />"), out);
        assertEquals(List.of(CDN + "/" + ID + "/attachment.png"), requested);
    }

    @Test
    void anImageThatCantBeFetchedStaysAnAbsoluteLink() {
        String missing = "11111111-2222-4333-8444-555555555555";
        String in = "<picture><source type=\"image/jxl\" srcset=\"/" + missing + "/attachment.jxl\" />"
                + "<img src=\"/attachments/v1/files/" + missing + "/fallback-missing\" alt=\"\" /></picture>";
        String out = epub(new ArrayList<>()).packImages(in, new HashMap<>(), new Book(), null);
        assertEquals("<img src=\"" + GATEWAY + "/attachments/v1/files/" + missing + "/fallback-missing\" alt=\"\" />", out);
    }

    @Test
    void mimeTypeFromContentThenHeader() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0};
        byte[] gif = "GIF89a\u0001\u0000\u0001\u0000".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] webp = "RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        assertEquals("image/png", EPubGeneratorBookFile.mimeType(png, "image/jpeg"), "the content wins over the header");
        assertEquals("image/jpeg", EPubGeneratorBookFile.mimeType(jpeg, null));
        assertEquals("image/gif", EPubGeneratorBookFile.mimeType(gif, null));
        assertEquals("image/webp", EPubGeneratorBookFile.mimeType(webp, null));
        assertEquals("image/svg+xml", EPubGeneratorBookFile.mimeType("<svg/>".getBytes(), "image/svg+xml; charset=utf-8"));
        assertEquals("image/png", EPubGeneratorBookFile.mimeType("not an image".getBytes(), "text/plain"), "unknown: PNG");
    }

    @Test
    void extensionOfTheType() {
        assertEquals("jpg", EPubGeneratorBookFile.extensionOf("image/jpeg"));
        assertEquals("png", EPubGeneratorBookFile.extensionOf("image/png"));
        assertEquals("gif", EPubGeneratorBookFile.extensionOf("image/gif"));
        assertEquals("webp", EPubGeneratorBookFile.extensionOf("image/webp"));
        assertEquals("svg", EPubGeneratorBookFile.extensionOf("image/svg+xml"));
        assertEquals("png", EPubGeneratorBookFile.extensionOf("not a type"), "unknown: png");
    }
}
