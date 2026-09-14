package ws.furrify.worker.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;
import org.openapitools.model.BookDTO;
import org.openapitools.model.ArtistDTO;
import org.openapitools.model.ArtistNickname;
import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
class EPubGeneratorBookFile extends BookFileWorkerGenerator {

    public EPubGeneratorBookFile(
            BookV1RestControllerApiClient bookV1RestControllerApiClient,
            BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient,
            BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient,
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(bookV1RestControllerApiClient, bookChapterV1RestControllerApiClient, bookChapterVersionV1RestControllerApiClient, attachmentFileV1RestControllerApiClient);
    }

    @Override
    protected String getContentType() {
        return "application/epub+zip";
    }

    @Override
    public String getExtension() {
        return "epub";
    }

    /**
     * Generates a fully packaged EPUB 2/3 compatible eBook file.
     * The file includes an overview page, multiple chapter HTML files, and embedded CSS resources.
     *
     * @param bookDto  The book metadata
     * @param chapters The list of chapters and their corresponding latest content versions
     * @param task     The worker task metadata
     * @return A temporary File containing the generated EPUB package
     */
    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO task) {
        log.debug("Generating EPUB for book '{}' with {} chapters.", bookDto.getTitle(), chapters.size());
        
        // 1. Initialize EPUB Book and add basic metadata
        Book epubBook = new Book();
        epubBook.getMetadata().addTitle(bookDto.getTitle());

        // Select the highest-priority nickname for each artist and add them as authors
        if (bookDto.getArtists() != null) {
            for (ArtistDTO artist : bookDto.getArtists()) {
                if (artist.getNicknames() != null && !artist.getNicknames().isEmpty()) {
                    ArtistNickname nickname = artist.getNicknames().stream()
                            .max(Comparator.comparing(n -> n.getPriority() != null ? n.getPriority() : 0))
                            .orElse(artist.getNicknames().get(0));
                    epubBook.getMetadata().addAuthor(new Author(nickname.getNickname()));
                }
            }
        }

        // 2. Generate Overview HTML page containing book statistics and metadata
        StringBuilder overviewHtml = new StringBuilder();
        overviewHtml.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<title>Overview</title>\n</head>\n<body>\n");
        overviewHtml.append("<h1>").append(bookDto.getTitle() != null ? bookDto.getTitle() : "Overview").append("</h1>\n");
        
        if (bookDto.getDescriptionHtml() != null) {
            overviewHtml.append("<div class=\"description\">").append(bookDto.getDescriptionHtml()).append("</div>\n");
        }
        
        overviewHtml.append("<ul>\n");
        if (bookDto.getArtists() != null && !bookDto.getArtists().isEmpty()) {
            java.util.List<String> authorNames = new java.util.ArrayList<>();
            for (ArtistDTO artist : bookDto.getArtists()) {
                if (artist.getNicknames() != null && !artist.getNicknames().isEmpty()) {
                    ArtistNickname nickname = artist.getNicknames().stream()
                            .max(Comparator.comparing(n -> n.getPriority() != null ? n.getPriority() : 0))
                            .orElse(artist.getNicknames().get(0));
                    authorNames.add(nickname.getNickname());
                }
            }
            if (!authorNames.isEmpty()) {
                overviewHtml.append("<li><strong>Artists:</strong> ").append(String.join(", ", authorNames)).append("</li>\n");
            }
        }
        
        if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
            java.util.List<String> tagNames = bookDto.getTags().stream().map(org.openapitools.model.TagDTO::getName).toList();
            overviewHtml.append("<li><strong>Tags:</strong> ").append(String.join(", ", tagNames)).append("</li>\n");
        }
        
        if (bookDto.getTotalWordCount() != null) {
            overviewHtml.append("<li><strong>Word Count:</strong> ").append(bookDto.getTotalWordCount()).append("</li>\n");
        }
        
        overviewHtml.append("<li><strong>Chapters:</strong> ").append(chapters.size()).append("</li>\n");
        
        if (bookDto.getStatus() != null) {
            String formattedStatus = java.util.Arrays.stream(bookDto.getStatus().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(java.util.stream.Collectors.joining(" "));
            overviewHtml.append("<li><strong>Status:</strong> ").append(formattedStatus).append("</li>\n");
        }
        if (bookDto.getRating() != null) {
            String formattedRating = java.util.Arrays.stream(bookDto.getRating().name().split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(java.util.stream.Collectors.joining(" "));
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
        
        // Add the overview HTML as the first section in the EPUB
        epubBook.addSection("Overview", new Resource(overviewHtml.toString().getBytes(StandardCharsets.UTF_8), "overview.html"));

        // Keep track of extracted CSS stylesheets to avoid creating duplicates in the EPUB package
        Map<String, String> cssMap = new HashMap<>();
        int cssCounter = 1;

        // 3. Process each chapter and add it to the EPUB
        for (ChapterData chapterData : chapters) {
            var chapter = chapterData.getChapter();
            var latestVersion = chapterData.getLatestVersion();
            log.debug("Adding chapter '{}' (number: {}) to EPUB.", chapter.getTitle(), chapter.getChapterNumber());

            // Extract and register chapter stylesheet as an independent EPUB resource if one exists
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

            // Build the individual chapter HTML file
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<title>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter").append("</title>\n");
            
            // Link the chapter's specific stylesheet
            if (cssFilename != null) {
                html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(cssFilename).append("\" />\n");
            }
            
            html.append("</head>\n<body>\n");
            html.append("<h2>").append(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber()).append("</h2>\n");
            
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
            html.append("</body>\n</html>");

            // Add the chapter HTML file to the EPUB spine and TOC
            epubBook.addSection(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapter.getChapterNumber(), new Resource(html.toString().getBytes(StandardCharsets.UTF_8), "chapter" + chapter.getChapterNumber() + ".html"));
        }

        // 4. Package everything into a temporary EPUB file
        File tmpFile = new File("/tmp/" + UUID.randomUUID() + ".epub");
        try {
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(epubBook, new FileOutputStream(tmpFile));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write epub file", e);
        }

        return tmpFile;
    }
}
