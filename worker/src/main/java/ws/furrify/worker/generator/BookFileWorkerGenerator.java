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
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.BookChapterDTO;
import org.openapitools.model.BookChapterVersionDTO;
import org.openapitools.model.BookDTO;
import ws.furrify.core.shared.StandardMultipartFile;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class BookFileWorkerGenerator {

    protected final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

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
            AttachmentFileDTO attachmentResponse = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerSave(tmpFile.getName(), multipartFile).getBody();
            if (attachmentResponse != null) {
                return attachmentResponse.getId();
            } else {
                log.error("Failed to upload generated file, response body was null (possibly an API error)");

                throw new RuntimeException("Failed to upload generated file, response body was null (possibly an API error)");
            }
        } catch (Exception e) {
            log.error("Failed to upload generated file: {}", e.getMessage());

            throw new RuntimeException("Failed to upload generated file");
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
