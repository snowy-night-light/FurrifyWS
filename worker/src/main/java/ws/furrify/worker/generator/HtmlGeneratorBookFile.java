package ws.furrify.worker.generator;

import org.openapitools.model.ArtistDTO;
import org.openapitools.model.ArtistNickname;
import org.openapitools.model.BookDTO;
import org.openapitools.model.TagDTO;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
class HtmlGeneratorBookFile extends BookFileWorkerGenerator {

    public HtmlGeneratorBookFile(
            ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
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

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8))) {

            writer.write("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\" />\n");

            String bookTitle = bookDto.getTitle() != null ? bookDto.getTitle() : "Untitled Book";
            writer.write("<title>" + bookTitle + "</title>\n");

            // Handle CSS: Gather all unique stylesheets from chapters and embed them in a single <style> block
            // to ensure chapters are styled properly while avoiding duplicate CSS rules in the HTML head.
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

            // Add Title Header
            writer.write("<h1>" + bookTitle + "</h1>\n");

            // Add overview section containing book metadata
            writer.write("<div class=\"overview\">\n");

            if (bookDto.getDescriptionHtml() != null) {
                writer.write("<div class=\"description\">" + bookDto.getDescriptionHtml() + "</div>\n");
            }

            writer.write("<ul>\n");

            // Add Authors inside overview list, selecting the highest-priority nickname for each artist
            if (bookDto.getArtists() != null && !bookDto.getArtists().isEmpty()) {
                List<String> authorNames = new ArrayList<>();
                for (ArtistDTO artist : bookDto.getArtists()) {
                    if (artist.getNicknames() != null && !artist.getNicknames().isEmpty()) {
                        ArtistNickname nickname = artist.getNicknames().stream()
                                .max(Comparator.comparing(n -> n.getPriority() != null ? n.getPriority() : 0))
                                .orElse(artist.getNicknames().get(0));
                        authorNames.add(nickname.getNickname());
                    }
                }
                if (!authorNames.isEmpty()) {
                    writer.write("<li><strong>Artists:</strong> " + String.join(", ", authorNames) + "</li>\n");
                }
            }

            // Add Tags
            if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
                List<String> tagNames = bookDto.getTags().stream().map(TagDTO::getName).toList();
                writer.write("<li><strong>Tags:</strong> " + String.join(", ", tagNames) + "</li>\n");
            }

            // Add Stats (Word Count, Chapters, Status, Rating, Views, Likes, Dislikes)
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

            // Add Chapters sequentially
            for (ChapterData chapterData : chapters) {
                var chapter = chapterData.getChapter();
                var latestVersion = chapterData.getLatestVersion();

                writer.write("<div class=\"chapter\" id=\"chapter-" + chapter.getChapterNumber() + "\">\n");
                writer.write("<h3>" + (chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()) + "</h3>\n");

                // Inject author notes at the start of the chapter if present
                if (latestVersion.getAuthorNotesStart() != null && !latestVersion.getAuthorNotesStart().isBlank()) {
                    writer.write("<div class=\"author-notes-start\"><i><b>Author notes:</b><br/>" + latestVersion.getAuthorNotesStart() + "</i></div>\n");
                }

                // Inject the main chapter HTML content
                if (latestVersion.getContentHtml() != null) {
                    writer.write("<div class=\"content\">" + latestVersion.getContentHtml() + "</div>\n");
                }

                // Inject author notes at the end of the chapter if present
                if (latestVersion.getAuthorNotesEnd() != null && !latestVersion.getAuthorNotesEnd().isBlank()) {
                    writer.write("<div class=\"author-notes-end\"><i><b>Author notes:</b><br/>" + latestVersion.getAuthorNotesEnd() + "</i></div>\n");
                }

                writer.write("</div>\n<hr/>\n");
            }

            writer.write("</body>\n</html>");

        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file", e);
        }

        return tmpFile;
    }
}
