package ws.furrify.worker.generator;

import org.openapitools.model.ArtistDTO;
import org.openapitools.model.ArtistNickname;
import org.openapitools.model.BookDTO;
import org.springframework.stereotype.Component;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
class HtmlGeneratorBookFile extends BookFileWorkerGenerator {

    public HtmlGeneratorBookFile(
            BookV1RestControllerApiClient bookV1RestControllerApiClient,
            BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient,
            BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient,
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(bookV1RestControllerApiClient, bookChapterV1RestControllerApiClient, bookChapterVersionV1RestControllerApiClient, attachmentFileV1RestControllerApiClient);
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
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO task) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
        
        String bookTitle = bookDto.getTitle() != null ? bookDto.getTitle() : "Untitled Book";
        html.append("<title>").append(bookTitle).append("</title>\n");
        
        // Handle CSS: Gather all unique stylesheets from chapters and embed them in a single <style> block
        // to ensure chapters are styled properly while avoiding duplicate CSS rules in the HTML head.
        Set<String> uniqueStyles = new HashSet<>();
        for (ChapterData chapterData : chapters) {
            var version = chapterData.getLatestVersion();
            if (version.getContentStylesheet() != null && !version.getContentStylesheet().isBlank()) {
                uniqueStyles.add(version.getContentStylesheet());
            }
        }
        
        if (!uniqueStyles.isEmpty()) {
            html.append("<style>\n");
            for (String style : uniqueStyles) {
                html.append(style).append("\n");
            }
            html.append("</style>\n");
        }
        
        html.append("</head>\n<body>\n");
        
        // Add Title Header
        html.append("<h1>").append(bookTitle).append("</h1>\n");
        
        // Add overview section containing book metadata
        html.append("<div class=\"overview\">\n");
        
        if (bookDto.getDescriptionHtml() != null) {
            html.append("<div class=\"description\">").append(bookDto.getDescriptionHtml()).append("</div>\n");
        }
        
        html.append("<ul>\n");
        
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
                html.append("<li><strong>Artists:</strong> ").append(String.join(", ", authorNames)).append("</li>\n");
            }
        }
        
        // Add Tags
        if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
            List<String> tagNames = bookDto.getTags().stream().map(org.openapitools.model.TagDTO::getName).toList();
            html.append("<li><strong>Tags:</strong> ").append(String.join(", ", tagNames)).append("</li>\n");
        }
        
        // Add Stats (Word Count, Chapters, Status, Rating, Views, Likes, Dislikes)
        if (bookDto.getTotalWordCount() != null) {
            html.append("<li><strong>Word Count:</strong> ").append(bookDto.getTotalWordCount()).append("</li>\n");
        }
        
        html.append("<li><strong>Chapters:</strong> ").append(chapters.size()).append("</li>\n");
        
        if (bookDto.getStatus() != null) {
            String formattedStatus = java.util.Arrays.stream(bookDto.getStatus().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(java.util.stream.Collectors.joining(" "));
            html.append("<li><strong>Status:</strong> ").append(formattedStatus).append("</li>\n");
        }
        if (bookDto.getRating() != null) {
            String formattedRating = java.util.Arrays.stream(bookDto.getRating().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(java.util.stream.Collectors.joining(" "));
            html.append("<li><strong>Rating:</strong> ").append(formattedRating).append("</li>\n");
        }
        if (bookDto.getViews() != null) {
            html.append("<li><strong>Views:</strong> ").append(bookDto.getViews()).append("</li>\n");
        }
        if (bookDto.getLikes() != null && bookDto.getLikes().isPresent() && bookDto.getLikes().get() != null) {
            html.append("<li><strong>Likes:</strong> ").append(bookDto.getLikes().get()).append("</li>\n");
        }
        if (bookDto.getDislikes() != null && bookDto.getDislikes().isPresent() && bookDto.getDislikes().get() != null) {
            html.append("<li><strong>Dislikes:</strong> ").append(bookDto.getDislikes().get()).append("</li>\n");
        }
        
        html.append("</ul>\n");
        html.append("</div>\n");
        
        html.append("<hr/>\n");
        
        // Add Chapters sequentially
        for (ChapterData chapterData : chapters) {
            var chapter = chapterData.getChapter();
            var latestVersion = chapterData.getLatestVersion();
            
            html.append("<div class=\"chapter\" id=\"chapter-").append(chapter.getChapterNumber()).append("\">\n");
            html.append("<h3>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()).append("</h3>\n");
            
            // Inject author notes at the start of the chapter if present
            if (latestVersion.getAuthorNotesStart() != null && !latestVersion.getAuthorNotesStart().isBlank()) {
                html.append("<div class=\"author-notes-start\"><i><b>Author notes:</b><br/>").append(latestVersion.getAuthorNotesStart()).append("</i></div>\n");
            }
            
            // Inject the main chapter HTML content
            if (latestVersion.getContentHtml() != null) {
                html.append("<div class=\"content\">").append(latestVersion.getContentHtml()).append("</div>\n");
            }
            
            // Inject author notes at the end of the chapter if present
            if (latestVersion.getAuthorNotesEnd() != null && !latestVersion.getAuthorNotesEnd().isBlank()) {
                html.append("<div class=\"author-notes-end\"><i><b>Author notes:</b><br/>").append(latestVersion.getAuthorNotesEnd()).append("</i></div>\n");
            }
            
            html.append("</div>\n<hr/>\n");
        }
        
        html.append("</body>\n</html>");

        // Write the final assembled HTML string to a temporary file
        File tmpFile = new File("/tmp/" + UUID.randomUUID().toString() + ".html");
        try {
            Files.writeString(tmpFile.toPath(), html.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file", e);
        }
        
        return tmpFile;
    }
}
