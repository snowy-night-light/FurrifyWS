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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.BookDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookFileWorkerGeneratorTest {

    private AttachmentFileV1RestControllerApiClient apiClient;
    private TestBookFileWorkerGenerator generator;
    private File createdFile;

    private static class TestBookFileWorkerGenerator extends BookFileWorkerGenerator {
        private final File fileToReturn;

        public TestBookFileWorkerGenerator(AttachmentFileV1RestControllerApiClient client, File fileToReturn) {
            super(client);
            this.fileToReturn = fileToReturn;
        }

        @Override
        public String getExtension() {
            return "test";
        }

        @Override
        protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, BookFileUserWorkerTaskDTO task) {
            return fileToReturn;
        }

        @Override
        protected String getContentType() {
            return "application/x-test";
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        apiClient = mock(AttachmentFileV1RestControllerApiClient.class);
        createdFile = Files.createTempFile("test-generator-", ".test").toFile();
        Files.writeString(createdFile.toPath(), "test content");
        generator = new TestBookFileWorkerGenerator(apiClient, createdFile);
    }

    @Test
    @DisplayName("generate successfully uploads file and deletes temporary file")
    void generateSuccess() {
        UUID expectedId = UUID.randomUUID();
        AttachmentFileDTO responseDto = new AttachmentFileDTO();
        responseDto.setId(expectedId);

        when(apiClient.attachmentFileV1RestControllerSaveWithUpload(eq(createdFile.getName()), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        BookFileUserWorkerTaskDTO task = BookFileUserWorkerTaskDTO.builder().id(UUID.randomUUID()).build();
        BookDTO bookDto = new BookDTO();

        UUID resultId = generator.generate(task, bookDto, Collections.emptyList());

        assertEquals(expectedId, resultId);
        assertFalse(createdFile.exists(), "Temporary file should have been deleted");

        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(apiClient).attachmentFileV1RestControllerSaveWithUpload(eq(createdFile.getName()), fileCaptor.capture());
        MultipartFile captured = fileCaptor.getValue();
        assertEquals("file", captured.getName());
        assertEquals(createdFile.getName(), captured.getOriginalFilename());
        assertEquals("application/x-test", captured.getContentType());
    }

    @Test
    @DisplayName("generate throws RuntimeException with cause when upload fails")
    void generateUploadFailure() {
        RuntimeException feignError = new RuntimeException("500 Internal Server Error");
        when(apiClient.attachmentFileV1RestControllerSaveWithUpload(eq(createdFile.getName()), any(MultipartFile.class)))
                .thenThrow(feignError);

        BookFileUserWorkerTaskDTO task = BookFileUserWorkerTaskDTO.builder().id(UUID.randomUUID()).build();
        BookDTO bookDto = new BookDTO();

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                generator.generate(task, bookDto, Collections.emptyList()));

        assertTrue(thrown.getMessage().contains("Failed to upload generated file"));
        assertSame(feignError, thrown.getCause());
        assertFalse(createdFile.exists(), "Temporary file should have been deleted even on failure");
    }

    @Test
    @DisplayName("generate throws RuntimeException when response body is null")
    void generateNullResponseBody() {
        when(apiClient.attachmentFileV1RestControllerSaveWithUpload(eq(createdFile.getName()), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok(null));

        BookFileUserWorkerTaskDTO task = BookFileUserWorkerTaskDTO.builder().id(UUID.randomUUID()).build();
        BookDTO bookDto = new BookDTO();

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                generator.generate(task, bookDto, Collections.emptyList()));

        assertTrue(thrown.getMessage().contains("response body was null"));
        assertFalse(createdFile.exists(), "Temporary file should have been deleted even when response is null");
    }
}
