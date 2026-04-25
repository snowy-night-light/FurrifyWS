package ws.furrify.attachment.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.MimeType;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.attachment.AttachmentApplication;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.AttachmentFileRepository;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHash;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.CreateAttachmentFileRequest;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.strategy.storage.MockFileMassStorageStrategy;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

@SpringBootTest(classes = AttachmentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AttachmentFileV1RestControllerTest extends BaseCrudControllerTest<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest, PatchAttachmentFileRequest> {

    @Autowired
    private AttachmentFileRepository attachmentFileRepository;

    @MockitoBean
    private FileMassStorageStrategy fileMassStorageStrategy;

    @BeforeEach
    protected void beforeEach() {
        MockFileMassStorageStrategy mockFileMassStorageStrategy = new MockFileMassStorageStrategy();

        Mockito.when(fileMassStorageStrategy.uploadFile(any(), any(), any(), anyBoolean()))
                .thenAnswer(invocation -> mockFileMassStorageStrategy.uploadFile(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3)));
        Mockito.when(fileMassStorageStrategy.getStorageServiceId())
                .thenReturn(mockFileMassStorageStrategy.getStorageServiceId());
    }

    @Autowired
    protected AttachmentFileV1RestControllerTest(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/storage/files";
    }

    protected AttachmentFileDTO create(CreateAttachmentFileRequest createAttachmentFileRequest, File file) {
        return given()
                .multiPart("file", file)
                .multiPart("fileName", createAttachmentFileRequest.getFileName())
                .when()
                .post(this.basePath)
                .then()
                .log()
                .all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(this.dtoClass);
    }

    protected AttachmentFileDTO patch(UUID id, PatchAttachmentFileRequest patchAttachmentFileRequest, File file) {
        return given()
                .pathParam("id", id)
                .multiPart("file", file)
                .multiPart("fileName", patchAttachmentFileRequest.getFileName().get())
                .when()
                .patch(this.basePath + "/{id}")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(this.dtoClass);
    }

    @Override
    @Test
    protected void testCreate() throws IOException, URISyntaxException {
        String filename = "test.png";

        URL resourceUrl = getClass().getClassLoader().getResource("files/image/example.png");
        assert resourceUrl != null;
        File file = new File(resourceUrl.toURI());

        CreateAttachmentFileRequest request = new CreateAttachmentFileRequest();
        request.setFileName(filename);


        AttachmentFileDTO createdAttachmentFile = this.create(request, file);

        assertAll(() -> {
            assertNotNull(createdAttachmentFile);
            assertEquals(filename, createdAttachmentFile.getFileName());
            assertFalse(createdAttachmentFile.getFileHashes().isEmpty());
            assertNotNull(createdAttachmentFile.getFileSize());
            assertTrue(createdAttachmentFile.getFileSize() > 0L);
            assertNotNull(createdAttachmentFile.getFileUri());
            assertNotNull(createdAttachmentFile.getThumbnailUri());
            assertEquals(FileUploadStatus.UPLOADED, createdAttachmentFile.getUploadStatus());
            assertEquals(MimeType.valueOf(MediaType.IMAGE_PNG_VALUE), createdAttachmentFile.getMimeType());
            assertEquals("MockFileMassStorageStrategy", createdAttachmentFile.getStorageServiceId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        AttachmentFile attachmentFile = attachmentFileRepository.save(
                AttachmentFile.builder()
                        .fileUri(URI.create("https://example.com/test.png"))
                        .thumbnailUri(URI.create("https://example.com/test.png"))
                        .uploadStatus(FileUploadStatus.UPLOADED)
                        .fileName("test.png")
                        .fileHashes(List.of(new AttachmentFileHash("test", AttachmentFileHashType.SHA256)))
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build()
        );

        AttachmentFileDTO attachmentFileDTO = super.findById(attachmentFile.getId());

        assertAll(() -> {
            assertNotNull(attachmentFileDTO);
            assertEquals(attachmentFile.getId(), attachmentFileDTO.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        attachmentFileRepository.save(
                AttachmentFile.builder()
                        .fileUri(URI.create("https://example.com/test.png"))
                        .thumbnailUri(URI.create("https://example.com/test.png"))
                        .uploadStatus(FileUploadStatus.UPLOADED)
                        .fileName("test.png")
                        .fileHashes(List.of(new AttachmentFileHash("test", AttachmentFileHashType.SHA256)))
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build()
        );

        attachmentFileRepository.save(
                AttachmentFile.builder()
                        .fileUri(URI.create("https://example.com/test2.png"))
                        .thumbnailUri(URI.create("https://example.com/test2.png"))
                        .uploadStatus(FileUploadStatus.UPLOADED)
                        .fileName("test2.png")
                        .fileHashes(List.of(new AttachmentFileHash("test2", AttachmentFileHashType.SHA256)))
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build()
        );

        Page<AttachmentFileDTO> attachmentFiles = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(attachmentFiles);
            assertEquals(2, attachmentFiles.getContent().size());
        });
    }

    @Override
    @Test
    @SneakyThrows
    protected void testPatch() {
        String filename = "test.jpg";

        URL resourceUrl = getClass().getClassLoader().getResource("files/image/example.jpg");
        assert resourceUrl != null;
        File file = new File(resourceUrl.toURI());

        AttachmentFile attachmentFile = attachmentFileRepository.save(
                AttachmentFile.builder()
                        .fileUri(URI.create("https://example.com/test.png"))
                        .thumbnailUri(URI.create("https://example.com/test.png"))
                        .uploadStatus(FileUploadStatus.UPLOADED)
                        .fileName("test.png")
                        .fileHashes(List.of(new AttachmentFileHash("test", AttachmentFileHashType.SHA256)))
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build()
        );

        PatchAttachmentFileRequest request = new PatchAttachmentFileRequest();
        request.setFileName(JsonNullable.of(filename));

        AttachmentFileDTO updatedAttachmentFile = this.patch(attachmentFile.getId(), request, file);

        assertAll(() -> {
            assertNotNull(updatedAttachmentFile);
            assertEquals(attachmentFile.getId(), updatedAttachmentFile.getId());
            assertEquals(filename, updatedAttachmentFile.getFileName());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        AttachmentFile attachmentFile = attachmentFileRepository.save(
                AttachmentFile.builder()
                        .fileUri(URI.create("https://example.com/test.png"))
                        .thumbnailUri(URI.create("https://example.com/test.png"))
                        .uploadStatus(FileUploadStatus.UPLOADED)
                        .fileName("test.png")
                        .fileHashes(List.of(new AttachmentFileHash("test", AttachmentFileHashType.SHA256)))
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build()
        );

        assertDoesNotThrow(() -> super.delete(attachmentFile.getId()));
    }
}
