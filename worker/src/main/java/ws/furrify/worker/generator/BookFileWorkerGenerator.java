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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.beans.factory.annotation.Value;
import ws.furrify.core.shared.StandardMultipartFile;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public abstract class BookFileWorkerGenerator {

    protected final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    /** Gateway route of the attachment service; its fallback endpoint serves images in their original format. */
    public static final String ATTACHMENTS_GATEWAY_PREFIX = "/attachments/";

    /** {@code src}/{@code srcset} of {@code <img>}/{@code <source>}; stored content is sanitized, which writes double-quoted attributes. */
    private static final Pattern IMAGE_URL_ATTRIBUTE = Pattern.compile(
            "(<(?:img|source)\\b[^>]*?\\b(?:src|srcset)\\s*=\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);

    @Value("${FURRIFY_CDN_URL:}")
    protected String cdnUrl;

    @Value("${FURRIFY_GATEWAY_URL:}")
    protected String gatewayUrl;

    /**
     * Absolute URL of a path content stores relative: {@code /attachments/...} (fallback endpoint,
     * via {@code FURRIFY_GATEWAY_URL}) or {@code /<id>/...} (a file on {@code FURRIFY_CDN_URL}).
     * Null for anything else (absolute URLs, data URIs, protocol-relative) or a missing base URL.
     */
    protected String absoluteUrl(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            return null;
        }
        String base = path.startsWith(ATTACHMENTS_GATEWAY_PREFIX) ? gatewayBase() : cdnBase();
        return base.isEmpty() ? null : base + path;
    }

    /** Every relative image URL ({@code <img src>}, {@code <source srcset>}) made absolute; others left alone. */
    protected String withAbsoluteImageUrls(String html) {
        if (html == null || (!html.contains("<img") && !html.contains("<source"))) {
            return html;
        }
        Matcher m = IMAGE_URL_ATTRIBUTE.matcher(html);
        StringBuilder out = new StringBuilder(html.length());
        while (m.find()) {
            String url = absoluteUrl(m.group(2).replace("&amp;", "&"));
            String replacement = url == null ? m.group(0) : m.group(1) + url.replace("&", "&amp;") + m.group(3);
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    /** Fallback endpoint of an attachment (its original format), absolute; null without FURRIFY_GATEWAY_URL. */
    protected String fallbackUrl(UUID attachmentId) {
        return absoluteUrl(ATTACHMENTS_GATEWAY_PREFIX + "v1/files/" + attachmentId + "/fallback");
    }

    protected String gatewayBase() {
        String gateway = gatewayUrl == null ? "" : gatewayUrl.trim();
        while (gateway.endsWith("/")) gateway = gateway.substring(0, gateway.length() - 1);
        return gateway;
    }

    protected String cdnBase() {
        String cdn = cdnUrl == null ? "" : cdnUrl.trim();
        while (cdn.endsWith("/")) cdn = cdn.substring(0, cdn.length() - 1);
        return cdn;
    }

    abstract public String getExtension();


    /**
     * Generates and uploads the book file using pre-fetched data.
     * Use this overload from the task orchestrator to avoid re-fetching chapter data for every generator.
     */
    public UUID generate(BookFileUserWorkerTaskDTO task, BookDTO bookDto, List<ChapterData> chapterDataList) {
        File tmpFile = generateFile(bookDto, chapterDataList, task);
        tmpFile.deleteOnExit();

        // Delegate to the child class impl
        try {
            StandardMultipartFile multipartFile = new StandardMultipartFile(
                    "file", 
                    tmpFile.getName(), 
                    getContentType(), 
                    tmpFile
            );
            AttachmentFileDTO attachmentResponse = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerSaveWithUpload(tmpFile.getName(), multipartFile).getBody();
            if (attachmentResponse != null) {
                return attachmentResponse.getId();
            } else {
                log.error("Failed to upload generated file, response body was null (possibly an API error)");

                throw new RuntimeException("Failed to upload generated file, response body was null (possibly an API error)");
            }
        } catch (Exception e) {
            log.error("Failed to upload generated file", e);

            throw new RuntimeException("Failed to upload generated file: " + e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(tmpFile.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary file {}: {}", tmpFile.getAbsolutePath(), e.getMessage());
            }
        }
    }

    /**
     * Subclasses implement this to build the specific format (EPub, Mobi, HTML)
     *
     * @param bookDto The book data
     * @param chapters List of chapters with their latest versions
     * @param task The task entity to save errors and warns to
     * @return The generated file, which will be uploaded and then deleted
     */
    protected abstract File generateFile(BookDTO bookDto, List<ChapterData> chapters, BookFileUserWorkerTaskDTO task);

    /**
     * Subclasses provide the appropriate content type (e.g., application/epub+zip)
     *
     * @return content type string
     */
    protected abstract String getContentType();

    @Getter
    @RequiredArgsConstructor
    public static class ChapterData {
        private final BookChapterDTO chapter;
        private final BookChapterVersionDTO latestVersion;
    }
}
