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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
        
        String bookTitle = bookDto.getTitle() != null ? bookDto.getTitle() : "Untitled Book";
        html.append("<title>").append(bookTitle).append("</title>\n");
        
        // Handle CSS: Gather all unique stylesheets from chapters and embed them in a <style> block
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
        
        // Add Authors
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
                html.append("<h2>By: ").append(String.join(", ", authorNames)).append("</h2>\n");
            }
        }
        
        html.append("<hr/>\n");
        
        // Add Chapters
        for (ChapterData chapterData : chapters) {
            var chapter = chapterData.getChapter();
            var latestVersion = chapterData.getLatestVersion();
            
            html.append("<div class=\"chapter\" id=\"chapter-").append(chapter.getChapterNumber()).append("\">\n");
            html.append("<h3>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()).append("</h3>\n");
            
            if (latestVersion.getAuthorNotesStart() != null) {
                html.append("<div class=\"author-notes-start\">").append(latestVersion.getAuthorNotesStart()).append("</div>\n");
            }
            if (latestVersion.getContentHtml() != null) {
                html.append("<div class=\"content\">").append(latestVersion.getContentHtml()).append("</div>\n");
            }
            if (latestVersion.getAuthorNotesEnd() != null) {
                html.append("<div class=\"author-notes-end\">").append(latestVersion.getAuthorNotesEnd()).append("</div>\n");
            }
            
            html.append("</div>\n<hr/>\n");
        }
        
        html.append("</body>\n</html>");

        File tmpFile = new File("/tmp/" + UUID.randomUUID().toString() + ".html");
        try {
            Files.write(tmpFile.toPath(), html.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file", e);
        }
        
        return tmpFile;
    }
}
