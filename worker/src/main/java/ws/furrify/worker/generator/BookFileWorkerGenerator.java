package ws.furrify.worker.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.shared.StandardMultipartFile;
import ws.furrify.core.utils.EntitySpecUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

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

    /**
     * Orchestrates the book file generation process by fetching book metadata, 
     * gathering chapter data, invoking the specific file generator, and uploading the result.
     *
     * @param task The worker task containing the book reference ID
     * @return The UUID of the uploaded attachment file
     * @throws RuntimeException if the book is not found or file upload fails
     */
    public UUID generate(BookFileUserWorkerTaskDTO task) {
        UUID bookId = task.getSourceBookReferenceId();
        
        // Fetch the book metadata from the storage service
        BookDTO bookDto = bookV1RestControllerApiClient.bookV1RestControllerGetById(bookId).getBody();
        if (bookDto == null) {
            throw new RuntimeException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(bookId));
        }

        // Fetch all chapters for this book
        String chapterSpec = "book.id = " + bookId;
        String encodedChapterSpec = EntitySpecUtils.encodeSpecToBase64(chapterSpec);
        Pageable pageable = new Pageable().page(0).size(1000);

        var chaptersResponse = bookChapterV1RestControllerApiClient.bookChapterV1RestControllerGetAllPaged(pageable, encodedChapterSpec).getBody();

        List<BookChapterDTO> chapters;
        if (chaptersResponse != null && chaptersResponse.getContent() != null) {
            chapters = chaptersResponse.getContent();
        } else {
            chapters = Collections.emptyList();
        }

        // Collect the latest version data for each chapter
        List<ChapterData> chapterDataList = new ArrayList<>();
        for (BookChapterDTO chapter : chapters) {
            String versionSpec = "chapter.id = " + chapter.getId();
            String encodedVersionSpec = EntitySpecUtils.encodeSpecToBase64(versionSpec);

            var versionsResponse = bookChapterVersionV1RestControllerApiClient.bookChapterVersionV1RestControllerGetAllPaged(pageable, encodedVersionSpec).getBody();

            List<BookChapterVersionDTO> versions;
            if (versionsResponse != null && versionsResponse.getContent() != null) {
                versions = versionsResponse.getContent();
            } else {
                versions = Collections.emptyList();
            }

            // Skip chapters without any text content versions
            if (versions.isEmpty()) {
                continue;
            }

            // Find the most recently updated version of the chapter
            BookChapterVersionDTO latestVersion = versions.stream()
                    .max(Comparator.comparing(v -> {
                        if (v.getContentUpdatedAt() != null) {
                            return v.getContentUpdatedAt();
                        } else {
                            return v.getCreatedAt() != null ? v.getCreatedAt() : java.time.ZonedDateTime.now().minusYears(100);
                        }
                    }))
                    .orElse(null);

            chapterDataList.add(new ChapterData(chapter, latestVersion));
        }

        // Delegate to the child class impl
        File tmpFile = generateFile(bookDto, chapterDataList, task);

        // Upload the generated file to the storage service as an attachment
        try {
            byte[] fileContent = Files.readAllBytes(tmpFile.toPath());
            MultipartFile multipartFile = new StandardMultipartFile("file", tmpFile.getName(), getContentType(), fileContent);

            AttachmentFileDTO attachmentResponse = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerSave(tmpFile.getName(), multipartFile).getBody();
            if (attachmentResponse != null) {
                return attachmentResponse.getId();
            } else {
                log.error("Failed to upload generated file, response body was null (possibly an API error)");

                throw new RuntimeException("Failed to upload generated file, response body was null (possibly an API error)");
            }
        } catch (Exception e) {
            log.error("Failed to read and upload generated file: {}", e.getMessage());

            throw new RuntimeException("Failed to read and upload generated file");
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
