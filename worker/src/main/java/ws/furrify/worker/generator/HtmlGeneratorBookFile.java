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

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.ArtistDTO;
import org.openapitools.model.ArtistNickname;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.BookDTO;
import org.openapitools.model.TagDTO;
import org.springframework.stereotype.Component;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
class HtmlGeneratorBookFile extends BookFileWorkerGenerator {

    public HtmlGeneratorBookFile(
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(attachmentFileV1RestControllerApiClient);
    }

    @Override
    protected String getContentType() {
        return "text/html";
    }

    @Override
    public String getExtension() {
        return "html";
    }

    /**
     * Generates a single, contiguous HTML file representing the entire book.
     * The file includes an overview/metadata section, embedded CSS, and all chapter contents.
     *
     * @param bookDto  The book metadata
     * @param chapters The list of chapters and their corresponding latest content versions
     * @param task     The worker task metadata
     * @return A temporary File containing the generated HTML document
     */
    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, BookFileUserWorkerTaskDTO task) {
        File tmpFile = new File("/tmp/" + UUID.randomUUID() + ".html");
        tmpFile.deleteOnExit();

        String coverHtml = coverMarkup(bookDto);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8))) {

            writer.write("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\" />\n");

            String bookTitle = bookDto.getTitle() != null ? bookDto.getTitle() : "Untitled Book";
            writer.write("<title>" + bookTitle + "</title>\n");

            Set<String> uniqueStyles = new LinkedHashSet<>();
            for (ChapterData chapterData : chapters) {
                var version = chapterData.getLatestVersion();
                if (version.getContentStylesheet() != null && !version.getContentStylesheet().isBlank()) {
                    uniqueStyles.add(version.getContentStylesheet());
                }
            }

            if (!uniqueStyles.isEmpty()) {
                writer.write("<style>\n");
                for (String style : uniqueStyles) {
                    writer.write(style);
                    writer.write("\n");
                }
                writer.write("</style>\n");
            }

            writer.write("</head>\n<body>\n");

            writer.write("<h1>" + bookTitle + "</h1>\n");

            if (coverHtml != null) {
                writer.write(coverHtml);
            }

            writer.write("<div class=\"overview\">\n");

            if (bookDto.getDescriptionHtml() != null) {
                writer.write("<div class=\"description\">" + withCdnUrls(bookDto.getDescriptionHtml()) + "</div>\n");
            }

            writer.write("<ul>\n");

            if (bookDto.getArtists() != null && !bookDto.getArtists().isEmpty()) {
                List<String> authorNames = new ArrayList<>();
                for (ArtistDTO artist : bookDto.getArtists()) {
                    if (artist.getNicknames() != null && !artist.getNicknames().isEmpty()) {
                        ArtistNickname nickname = artist.getNicknames().stream()
                                .max(Comparator.comparing(n -> n.getPriority() != null ? n.getPriority() : 0))
                                .orElse(artist.getNicknames().getFirst());
                        authorNames.add(nickname.getNickname());
                    }
                }
                if (!authorNames.isEmpty()) {
                    writer.write("<li><strong>Artists:</strong> " + String.join(", ", authorNames) + "</li>\n");
                }
            }

            if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
                List<String> tagNames = bookDto.getTags().stream().map(TagDTO::getName).toList();
                writer.write("<li><strong>Tags:</strong> " + String.join(", ", tagNames) + "</li>\n");
            }

            if (bookDto.getTotalWordCount() != null) {
                writer.write("<li><strong>Word Count:</strong> " + bookDto.getTotalWordCount() + "</li>\n");
            }

            writer.write("<li><strong>Chapters:</strong> " + chapters.size() + "</li>\n");

            if (bookDto.getStatus() != null) {
                String formattedStatus = Arrays.stream(bookDto.getStatus().name().split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));
                writer.write("<li><strong>Status:</strong> " + formattedStatus + "</li>\n");
            }
            if (bookDto.getRating() != null) {
                String formattedRating = Arrays.stream(bookDto.getRating().name().split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));
                writer.write("<li><strong>Rating:</strong> " + formattedRating + "</li>\n");
            }
            if (bookDto.getViews() != null) {
                writer.write("<li><strong>Views:</strong> " + bookDto.getViews() + "</li>\n");
            }
            if (bookDto.getLikes() != null && bookDto.getLikes().isPresent() && bookDto.getLikes().get() != null) {
                writer.write("<li><strong>Likes:</strong> " + bookDto.getLikes().get() + "</li>\n");
            }
            if (bookDto.getDislikes() != null && bookDto.getDislikes().isPresent() && bookDto.getDislikes().get() != null) {
                writer.write("<li><strong>Dislikes:</strong> " + bookDto.getDislikes().get() + "</li>\n");
            }

            writer.write("</ul>\n");
            writer.write("</div>\n");

            writer.write("<hr/>\n");

            for (ChapterData chapterData : chapters) {
                var chapter = chapterData.getChapter();
                var latestVersion = chapterData.getLatestVersion();

                writer.write("<div class=\"chapter\" id=\"chapter-" + chapter.getChapterNumber() + "\">\n");
                writer.write("<h3>" + (chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()) + "</h3>\n");

                if (latestVersion.getAuthorNotesStart() != null && !latestVersion.getAuthorNotesStart().isBlank()) {
                    writer.write("<div class=\"author-notes-start\"><i><b>Author notes:</b><br/>" + withCdnUrls(latestVersion.getAuthorNotesStart()) + "</i></div>\n");
                }

                if (latestVersion.getContentHtml() != null) {
                    writer.write("<div class=\"content\">" + withCdnUrls(latestVersion.getContentHtml()) + "</div>\n");
                }

                if (latestVersion.getAuthorNotesEnd() != null && !latestVersion.getAuthorNotesEnd().isBlank()) {
                    writer.write("<div class=\"author-notes-end\"><i><b>Author notes:</b><br/>" + withCdnUrls(latestVersion.getAuthorNotesEnd()) + "</i></div>\n");
                }

                writer.write("</div>\n<hr/>\n");
            }

            writer.write("</body>\n</html>");

        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file", e);
        }

        return tmpFile;
    }

    /** Our images as absolute links: JPEG XL from the CDN, the fallback endpoint (via the gateway) for the rest. */
    String withCdnUrls(String html) {
        if (html != null && (html.contains("<img") || html.contains("<source")) && (cdnBase().isEmpty() || gatewayBase().isEmpty())) {
            log.warn("FURRIFY_CDN_URL or FURRIFY_GATEWAY_URL is not set: images in the HTML file keep their relative paths");
        }
        return withAbsoluteImageUrls(html);
    }

    /**
     * The cover as links (no embedded copy): JPEG XL from the CDN with its original format from
     * the fallback endpoint, or just the file when it isn't stored as JPEG XL. Null without a cover.
     */
    private String coverMarkup(BookDTO bookDto) {
        if (bookDto.getCover() == null || bookDto.getCover().getFileReferenceId() == null) {
            return null;
        }
        UUID fileId = bookDto.getCover().getFileReferenceId();
        String fallback = fallbackUrl(fileId);
        if (fallback == null) {
            log.warn("FURRIFY_GATEWAY_URL is not set: the HTML file has no cover");
            return null;
        }
        String style = " alt=\"Cover\" style=\"max-width: 100%; height: auto;\" />";
        String jxl = null;
        if (bookDto.getOwnerId() != null) {
            SecurityContextUtils.mockFeignClientSecurityContext(bookDto.getOwnerId());
        }
        try {
            AttachmentFileDTO file = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileId).getBody();
            if (file != null && file.getFileUri() != null && "image/jxl".equals(file.getMimeType())) {
                jxl = absoluteUrl(file.getFileUri().toString());
            }
        } catch (Exception e) {
            log.warn("Could not look up cover file {}, linking its fallback only: {}", fileId, e.getMessage());
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
        if (jxl == null) {
            return "<img src=\"" + fallback + "\"" + style + "\n";
        }
        return "<picture><source type=\"image/jxl\" srcset=\"" + jxl + "\" /><img src=\"" + fallback + "\"" + style + "</picture>\n";
    }
}
