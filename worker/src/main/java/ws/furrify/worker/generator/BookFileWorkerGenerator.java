package ws.furrify.worker.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.utils.EntitySpecUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public abstract class BookFileWorkerGenerator {

    protected final BookV1RestControllerApiClient bookV1RestControllerApiClient;
    protected final BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient;
    protected final BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient;
    protected final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    abstract public String getExtension();

    public UUID generate(UUID bookId) {
        BookDTO bookDto = bookV1RestControllerApiClient.bookV1RestControllerGetById(bookId).getBody();
        if (bookDto == null) {
            throw new RuntimeException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(bookId));
        }

        String chapterSpec = "book.id = " + bookId;
        String encodedChapterSpec = EntitySpecUtils.encodeSpecToBase64(chapterSpec);
        Pageable pageable = new Pageable().page(0).size(1000);

        var chaptersResponse = bookChapterV1RestControllerApiClient.bookChapterV1RestControllerGetAllPaged(pageable, encodedChapterSpec).getBody();
        List<BookChapterDTO> chapters = chaptersResponse != null && chaptersResponse.getContent() != null ? chaptersResponse.getContent() : Collections.emptyList();

        List<ChapterData> chapterDataList = new ArrayList<>();
        for (BookChapterDTO chapter : chapters) {
            String versionSpec = "chapter.id = " + chapter.getId();
            String encodedVersionSpec = EntitySpecUtils.encodeSpecToBase64(versionSpec);
            var versionsResponse = bookChapterVersionV1RestControllerApiClient.bookChapterVersionV1RestControllerGetAllPaged(pageable, encodedVersionSpec).getBody();
            List<BookChapterVersionDTO> versions = versionsResponse != null && versionsResponse.getContent() != null ? versionsResponse.getContent() : Collections.emptyList();

            if (versions.isEmpty()) continue;

            BookChapterVersionDTO latestVersion = versions.stream()
                    .max(Comparator.comparing(v -> v.getContentUpdatedAt() != null ? v.getContentUpdatedAt() : (v.getCreatedAt() != null ? v.getCreatedAt() : java.time.ZonedDateTime.now().minusYears(100))))
                    .orElse(null);

            chapterDataList.add(new ChapterData(chapter, latestVersion));
        }

        File tmpFile = generateFile(bookDto, chapterDataList);

        try {
            byte[] fileContent = Files.readAllBytes(tmpFile.toPath());
            MultipartFile multipartFile = new ws.furrify.core.shared.StandardMultipartFile("file", tmpFile.getName(), getContentType(), fileContent);

            AttachmentFileDTO attachmentResponse = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerSave(tmpFile.getName(), multipartFile).getBody();
            if (attachmentResponse != null) {
                return attachmentResponse.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read and upload generated file", e);
        } finally {
            try {
                Files.deleteIfExists(tmpFile.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary file {}: {}", tmpFile.getAbsolutePath(), e.getMessage());
            }
        }
        return null;
    }

    /**
     * Subclasses implement this to build the specific format (EPub, Mobi, HTML)
     *
     * @param bookDto The book data
     * @param chapters List of chapters with their latest versions
     * @return The generated file, which will be uploaded and then deleted
     */
    protected abstract File generateFile(BookDTO bookDto, List<ChapterData> chapters);

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
