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

import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.ArtistDTO;
import org.openapitools.model.ArtistNickname;
import org.openapitools.model.BookDTO;
import org.openapitools.model.TagDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.service.worker.AttachmentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
class EPubGeneratorBookFile extends BookFileWorkerGenerator {

    private final AttachmentService attachmentService;

    @Autowired
    public EPubGeneratorBookFile(
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, AttachmentService attachmentService) {
        super(attachmentFileV1RestControllerApiClient);
        this.attachmentService = attachmentService;
    }

    @Override
    protected String getContentType() {
        return "application/epub+zip";
    }

    @Override
    public String getExtension() {
        return "epub";
    }

    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, BookFileUserWorkerTaskDTO task) {
        log.debug("Generating EPUB for book '{}' with {} chapters.", bookDto.getTitle(), chapters.size());

        Book epubBook = new Book();
        String coverHref = null;

        if (bookDto.getCover() != null && bookDto.getCover().getFileReferenceId() != null) {
            if (bookDto.getOwnerId() != null) {
                SecurityContextUtils.mockFeignClientSecurityContext(bookDto.getOwnerId());
            }

            File coverFile = null;

            try {
                coverFile = attachmentService.downloadAttachment(bookDto.getCover(), "gbf-epub-cover-file-");

                String originalName = coverFile.getName();
                String extension = "";
                int extIndex = originalName.lastIndexOf('.');
                if (extIndex > 0) {
                    extension = originalName.substring(extIndex);
                }

                coverHref = "cover" + extension;

                Resource coverResource = new Resource(
                        new FileInputStream(coverFile),
                        coverHref
                );
                epubBook.setCoverImage(coverResource);
            } catch (Exception e) {
                log.error("Failed to fetch attachment file reference [id={}]: {}", bookDto.getCover().getFileReferenceId(), e.getMessage());
            } finally {
                if (coverFile != null) {
                    attachmentService.cleanupTmpFile(coverFile);
                }
                SecurityContextUtils.clearFeignClientSecurityContext();
            }
        }

        epubBook.getMetadata().addTitle(bookDto.getTitle());

        if (coverHref != null) {
            String coverHtml = "<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta charset=\"UTF-8\" />\n<title>Cover</title>\n" +
                    "<style type=\"text/css\">\n" +
                    "body { margin: 0; padding: 0; text-align: center; }\n" +
                    "img { max-width: 100%; max-height: 100vh; height: auto; }\n" +
                    "</style>\n" +
                    "</head>\n<body>\n" +
                    "<img src=\"" + coverHref + "\" alt=\"Cover\" />\n" +
                    "</body>\n</html>";

            epubBook.addSection("Cover", new Resource(coverHtml.getBytes(StandardCharsets.UTF_8), "cover.html"));
        }

        if (bookDto.getArtists() != null) {
            for (ArtistDTO artist : bookDto.getArtists()) {
                if (artist.getNicknames() != null && !artist.getNicknames().isEmpty()) {
                    ArtistNickname nickname = artist.getNicknames().stream()
                            .max(Comparator.comparing(n -> n.getPriority() != null ? n.getPriority() : 0))
                            .orElse(artist.getNicknames().getFirst());
                    epubBook.getMetadata().addAuthor(new Author(nickname.getNickname()));
                }
            }
        }

        StringBuilder overviewHtml = new StringBuilder();
        overviewHtml.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta charset=\"UTF-8\" />\n<title>Overview</title>\n</head>\n<body>\n");
        overviewHtml.append("<h1>").append(bookDto.getTitle() != null ? bookDto.getTitle() : "Overview").append("</h1>\n");

        if (bookDto.getDescriptionHtml() != null) {
            overviewHtml.append("<div class=\"description\">").append(bookDto.getDescriptionHtml()).append("</div>\n");
        }

        overviewHtml.append("<ul>\n");
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
                overviewHtml.append("<li><strong>Artists:</strong> ").append(String.join(", ", authorNames)).append("</li>\n");
            }
        }

        if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
            List<String> tagNames = bookDto.getTags().stream().map(TagDTO::getName).toList();
            overviewHtml.append("<li><strong>Tags:</strong> ").append(String.join(", ", tagNames)).append("</li>\n");
        }

        if (bookDto.getTotalWordCount() != null) {
            overviewHtml.append("<li><strong>Word Count:</strong> ").append(bookDto.getTotalWordCount()).append("</li>\n");
        }

        overviewHtml.append("<li><strong>Chapters:</strong> ").append(chapters.size()).append("</li>\n");

        if (bookDto.getStatus() != null) {
            String formattedStatus = Arrays.stream(bookDto.getStatus().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            overviewHtml.append("<li><strong>Status:</strong> ").append(formattedStatus).append("</li>\n");
        }
        if (bookDto.getRating() != null) {
            String formattedRating = Arrays.stream(bookDto.getRating().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            overviewHtml.append("<li><strong>Rating:</strong> ").append(formattedRating).append("</li>\n");
        }
        if (bookDto.getViews() != null) {
            overviewHtml.append("<li><strong>Views:</strong> ").append(bookDto.getViews()).append("</li>\n");
        }
        if (bookDto.getLikes() != null && bookDto.getLikes().isPresent() && bookDto.getLikes().get() != null) {
            overviewHtml.append("<li><strong>Likes:</strong> ").append(bookDto.getLikes().get()).append("</li>\n");
        }
        if (bookDto.getDislikes() != null && bookDto.getDislikes().isPresent() && bookDto.getDislikes().get() != null) {
            overviewHtml.append("<li><strong>Dislikes:</strong> ").append(bookDto.getDislikes().get()).append("</li>\n");
        }

        overviewHtml.append("</ul>\n");
        overviewHtml.append("</body>\n</html>");

        epubBook.addSection("Overview", new Resource(overviewHtml.toString().getBytes(StandardCharsets.UTF_8), "overview.html"));

        Map<String, String> cssMap = new HashMap<>();
        int cssCounter = 1;

        for (ChapterData chapterData : chapters) {
            var chapter = chapterData.getChapter();
            var latestVersion = chapterData.getLatestVersion();
            log.debug("Adding chapter '{}' (number: {}) to EPUB.", chapter.getTitle(), chapter.getChapterNumber());

            String cssFilename = null;
            if (latestVersion.getContentStylesheet() != null && !latestVersion.getContentStylesheet().isBlank()) {
                String css = latestVersion.getContentStylesheet();
                if (cssMap.containsKey(css)) {
                    cssFilename = cssMap.get(css);
                } else {
                    cssFilename = "style" + cssCounter++ + ".css";
                    cssMap.put(css, cssFilename);
                    epubBook.getResources().add(new Resource(css.getBytes(StandardCharsets.UTF_8), cssFilename));
                }
            }

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta charset=\"UTF-8\" />\n<title>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter").append("</title>\n");

            if (cssFilename != null) {
                html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(cssFilename).append("\" />\n");
            }

            html.append("</head>\n<body>\n");
            html.append("<h2>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()).append("</h2>\n");

            if (latestVersion.getAuthorNotesStart() != null && !latestVersion.getAuthorNotesStart().isBlank()) {
                html.append("<div class=\"author-notes-start\"><i><b>Author notes:</b><br/>").append(latestVersion.getAuthorNotesStart()).append("</i></div>\n");
            }

            if (latestVersion.getContentHtml() != null) {
                html.append("<div class=\"content\">").append(latestVersion.getContentHtml()).append("</div>\n");
            }

            if (latestVersion.getAuthorNotesEnd() != null && !latestVersion.getAuthorNotesEnd().isBlank()) {
                html.append("<div class=\"author-notes-end\"><i><b>Author notes:</b><br/>").append(latestVersion.getAuthorNotesEnd()).append("</i></div>\n");
            }
            html.append("</body>\n</html>");

            epubBook.addSection(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber(), new Resource(html.toString().getBytes(StandardCharsets.UTF_8), "chapter" + chapter.getChapterNumber() + ".html"));
        }

        File tmpFile = new File("/tmp/" + UUID.randomUUID() + ".epub");
        tmpFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(epubBook, fos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write epub file", e);
        }

        return tmpFile;
    }
}