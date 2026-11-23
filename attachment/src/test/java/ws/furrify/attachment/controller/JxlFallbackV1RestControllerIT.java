/*
 * furrify-attachment-service - Furrify Workspace Project
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
package ws.furrify.attachment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.attachment.AttachmentApplication;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.AttachmentFileRepository;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.service.file.jxl.JxlConverter;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseControllerTest;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = AttachmentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JxlFallbackV1RestControllerIT extends BaseControllerTest {

    /** First bytes of a PNG and of a JPEG. */
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    @Autowired
    private AttachmentFileRepository attachmentFileRepository;

    @Autowired
    private JxlConverter jxlConverter;

    @MockitoBean
    private FileMassStorageStrategy fileMassStorageStrategy;

    /** Stored files of the test: a stored URI's path is the file's absolute path. */
    @BeforeEach
    protected void beforeEach() {
        Mockito.when(fileMassStorageStrategy.resolve(any())).thenAnswer(invocation -> Path.of(((URI) invocation.getArgument(0)).getPath()));
    }

    @Autowired
    protected JxlFallbackV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/files";
    }

    @Test
    void testFallbackOfAFileNotStoredAsJxlIsTheFileItself() throws Exception {
        File png = copyOfResource("/files/image/example.png", ".png");
        AttachmentFile file = save("image/png", png, null);

        byte[] body = fetch("/{id}/fallback", file.getId(), "image/png");

        assertArrayEquals(Files.readAllBytes(png.toPath()), body);
    }

    @Test
    void testFallbackOfAJxlIsAPng() throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg"), "JPEG XL tools not installed");
        File png = copyOfResource("/files/image/example.png", ".png");
        File jxl = jxlConverter.convert(png, "image/png").orElseThrow().jxlFile();
        AttachmentFile file = save(JxlConverter.JXL_MIME_TYPE, jxl, null, "image/png");

        byte[] body = fetch("/{id}/fallback", file.getId(), "image/png");

        assertArrayEquals(PNG_SIGNATURE, java.util.Arrays.copyOf(body, PNG_SIGNATURE.length));
    }

    @Test
    void testThumbnailFallbackIsTheThumbnailJpeg() throws Exception {
        File thumbnail = copyOfResource("/files/image/example.jpg", ".jpg");
        AttachmentFile file = save("image/png", copyOfResource("/files/image/example.png", ".png"), thumbnail);

        byte[] body = fetch("/{id}/thumbnail/fallback", file.getId(), "image/jpeg");

        assertArrayEquals(JPEG_SIGNATURE, java.util.Arrays.copyOf(body, JPEG_SIGNATURE.length));
    }

    @Test
    void testThumbnailFallbackWithoutThumbnailIsNotFound() throws Exception {
        AttachmentFile file = save("image/png", copyOfResource("/files/image/example.png", ".png"), null);

        given().pathParam("id", file.getId())
                .when().get(this.basePath + "/{id}/thumbnail/fallback")
                .then().log().all().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testFallbackOfUnknownFileIsNotFound() {
        given().pathParam("id", UUID.randomUUID())
                .when().get(this.basePath + "/{id}/fallback")
                .then().log().all().statusCode(HttpStatus.NOT_FOUND.value());
    }

    private byte[] fetch(String path, UUID id, String expectedType) {
        return given().pathParam("id", id)
                .when().get(this.basePath + path)
                .then().log().headers()
                .statusCode(HttpStatus.OK.value())
                .contentType(expectedType)
                .extract().asByteArray();
    }

    private AttachmentFile save(String mimeType, File stored, File thumbnail) {
        return save(mimeType, stored, thumbnail, null);
    }

    private AttachmentFile save(String mimeType, File stored, File thumbnail, String originalMimeType) {
        String extension = stored.getName().substring(stored.getName().lastIndexOf('.') + 1);
        return attachmentFileRepository.save(AttachmentFile.builder()
                .fileName("test." + extension)
                .fileExtension(extension)
                .mimeType(mimeType)
                .originalMimeType(originalMimeType)
                .fileUri(stored.toURI())
                .thumbnailUri(thumbnail != null ? thumbnail.toURI() : null)
                .uploadStatus(FileUploadStatus.UPLOADED)
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                .build());
    }

    private File copyOfResource(String resource, String suffix) throws Exception {
        File copy = Files.createTempFile("fallback-test-", suffix).toFile();
        copy.deleteOnExit();
        try (var in = getClass().getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            Files.copy(in, copy.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return copy;
    }

    private static boolean onPath(String tool) {
        for (String dir : System.getenv().getOrDefault("PATH", "").split(File.pathSeparator)) {
            if (new File(dir, tool).canExecute()) return true;
        }
        return false;
    }
}
