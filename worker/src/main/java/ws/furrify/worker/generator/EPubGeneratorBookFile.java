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
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
class EPubGeneratorBookFile extends BookFileWorkerGenerator {

    private static final long MAX_INLINE_IMAGE_BYTES = 25L * 1024 * 1024;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    @Autowired
    public EPubGeneratorBookFile(
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(attachmentFileV1RestControllerApiClient);
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
            // the cover in its original format (JPEG XL isn't an EPUB image type), packed as a file
            String url = fallbackUrl(bookDto.getCover().getFileReferenceId());
            Fetched cover = url == null ? null : fetchImage(url, task);
            if (cover != null) {
                coverHref = "cover." + cover.extension();
                epubBook.setCoverImage(new Resource(cover.bytes(), coverHref));
            } else if (url == null) {
                warn(task, "FURRIFY_GATEWAY_URL is not set: the EPUB has no cover");
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

        // image URL -> href of its file inside the EPUB (null = could not be fetched), shared by all chapters
        Map<String, String> packedImages = new HashMap<>();

        StringBuilder overviewHtml = new StringBuilder();
        overviewHtml.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta charset=\"UTF-8\" />\n<title>Overview</title>\n</head>\n<body>\n");
        overviewHtml.append("<h1>").append(bookDto.getTitle() != null ? bookDto.getTitle() : "Overview").append("</h1>\n");

        if (bookDto.getDescriptionHtml() != null) {
            overviewHtml.append("<div class=\"description\">").append(packImages(bookDto.getDescriptionHtml(), packedImages, epubBook, task)).append("</div>\n");
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
                html.append("<div class=\"author-notes-start\"><i><b>Author notes:</b><br/>").append(packImages(latestVersion.getAuthorNotesStart(), packedImages, epubBook, task)).append("</i></div>\n");
            }

            if (latestVersion.getContentHtml() != null) {
                html.append("<div class=\"content\">").append(packImages(latestVersion.getContentHtml(), packedImages, epubBook, task)).append("</div>\n");
            }

            if (latestVersion.getAuthorNotesEnd() != null && !latestVersion.getAuthorNotesEnd().isBlank()) {
                html.append("<div class=\"author-notes-end\"><i><b>Author notes:</b><br/>").append(packImages(latestVersion.getAuthorNotesEnd(), packedImages, epubBook, task)).append("</i></div>\n");
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

    private static final Pattern PICTURE = Pattern.compile("<picture\\b[^>]*>(.*?)</picture>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern IMG_SRC = Pattern.compile("<img\\b[^>]*?\\bsrc\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_TAG = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALT = Pattern.compile("\\balt\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    /** A downloaded image: its bytes, type and file extension. */
    record Fetched(byte[] bytes, String mimeType, String extension) {
    }

    /**
     * Pack our images into the EPUB as files, each once per book: a {@code <picture>} (JPEG XL +
     * fallback) becomes {@code <img src="images/...">} of its fallback (the original format; EPUB
     * readers don't show JPEG XL), a plain {@code <img>} of a CDN path packs that file. Images on
     * other hosts stay links; an image that can't be fetched keeps an absolute link (warning on the task).
     */
    String packImages(String html, Map<String, String> packed, Book epubBook, BookFileUserWorkerTaskDTO task) {
        if (html == null || !html.contains("<img")) {
            return html;
        }
        Matcher pictures = PICTURE.matcher(html);
        StringBuilder out = new StringBuilder(html.length());
        while (pictures.find()) {
            Matcher img = IMG_SRC.matcher(pictures.group(1));
            String replacement = pictures.group(0);
            if (img.find()) {
                Matcher alt = ALT.matcher(pictures.group(1));
                String altText = alt.find() ? alt.group(1) : "";
                String src = img.group(1).replace("&amp;", "&");
                String url = absoluteUrl(src);
                String href = url == null ? null : pack(url, packed, epubBook, task);
                String target = href != null ? href : (url != null ? url : src);
                replacement = "<img src=\"" + target.replace("&", "&amp;") + "\" alt=\"" + altText + "\" />";
            }
            pictures.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        pictures.appendTail(out);

        Matcher tags = IMG_TAG.matcher(out.toString());
        StringBuilder result = new StringBuilder(out.length());
        while (tags.find()) {
            String tag = tags.group(0);
            Matcher img = IMG_SRC.matcher(tag);
            if (img.find()) {
                String src = img.group(1).replace("&amp;", "&");
                String url = absoluteUrl(src); // only our relative paths; images/... and http(s) are left alone
                if (url != null) {
                    String href = pack(url, packed, epubBook, task);
                    String target = href != null ? href : url;
                    tag = tag.substring(0, img.start(1)) + target.replace("&", "&amp;") + tag.substring(img.end(1));
                }
            }
            tags.appendReplacement(result, Matcher.quoteReplacement(tag));
        }
        tags.appendTail(result);
        return result.toString();
    }

    /** Href of {@code url}'s image inside the EPUB, adding the file on first use; null when it can't be fetched. */
    private String pack(String url, Map<String, String> packed, Book epubBook, BookFileUserWorkerTaskDTO task) {
        if (packed.containsKey(url)) {
            return packed.get(url); // failures cached too: one warning per image
        }
        String href = null;
        Fetched image = fetchImage(url, task);
        if (image != null) {
            // named by content: the same image linked under different URLs is stored once
            href = "images/" + sha256Hex(image.bytes()).substring(0, 20) + "." + image.extension();
            if (epubBook.getResources().getByHref(href) == null) {
                epubBook.getResources().add(new Resource(image.bytes(), href));
            }
        }
        packed.put(url, href);
        return href;
    }

    /** Attempts per image: a busy or restarting attachment service (429, 5xx, dropped connection) is waited out. */
    private static final int FETCH_ATTEMPTS = 4;

    /** The image at {@code url}, or null (warning added). */
    Fetched fetchImage(String url, BookFileUserWorkerTaskDTO task) {
        try {
            HttpResponse<byte[]> response = null;
            for (int attempt = 1; ; attempt++) {
                try {
                    response = HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(url))
                            .timeout(Duration.ofSeconds(60)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
                    int status = response.statusCode();
                    if ((status != 429 && status < 500) || attempt == FETCH_ATTEMPTS) break;
                } catch (java.io.IOException e) {
                    if (attempt == FETCH_ATTEMPTS) throw e;
                }
                Thread.sleep(2_000L << (attempt - 1)); // 2s, 4s, 8s
            }
            byte[] body = response.body();
            if (response.statusCode() != 200 || body == null || body.length == 0) {
                warn(task, "Image " + url + " could not be packed into the EPUB (HTTP " + response.statusCode() + "), it stays a link");
                return null;
            }
            if (body.length > MAX_INLINE_IMAGE_BYTES) {
                warn(task, "Image " + url + " is too large to pack into the EPUB (" + body.length + " bytes), it stays a link");
                return null;
            }
            String mime = mimeType(body, response.headers().firstValue("Content-Type").orElse(null));
            return new Fetched(body, mime, extensionOf(mime));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            warn(task, "Image " + url + " could not be packed into the EPUB (" + e.getMessage() + "), it stays a link");
            return null;
        }
    }

    /** The usual extension of an image type ("jpg" for image/jpeg), "png" when Tika knows none. */
    static String extensionOf(String mimeType) {
        try {
            String extension = org.apache.tika.mime.MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
            if (extension != null && extension.length() > 1) return extension.substring(1); // ".jpg" -> "jpg"
        } catch (org.apache.tika.mime.MimeTypeException ignored) {
            // malformed type: below
        }
        return "png";
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Thread-safe; detects by the content's magic bytes. */
    private static final org.apache.tika.Tika TIKA = new org.apache.tika.Tika();

    /** An image type detected from the file's bytes (Tika), else the response's image/* content type, else PNG. */
    static String mimeType(byte[] b, String contentType) {
        String detected = TIKA.detect(b);
        if (detected != null && detected.startsWith("image/")) return detected;
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        }
        return "image/png";
    }

    private static void warn(BookFileUserWorkerTaskDTO task, String message) {
        log.warn(message);
        if (task != null) {
            if (task.getWarnings() == null) task.setWarnings(new ArrayList<>());
            task.getWarnings().add(message);
        }
    }
}
