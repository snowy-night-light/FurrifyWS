package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.AttachmentFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.media.MediaRepository;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.media.request.CreateMediaRequest;
import ws.furrify.storage.dto.media.request.PatchMediaRequest;
import ws.furrify.storage.mocks.MockSourceStrategyImpl;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MediaV1RestControllerTest extends BaseCrudControllerTest<Media, MediaDTO, CreateMediaRequest, PatchMediaRequest> {

    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private SourceRepository sourceRepository;

    @MockitoBean
    private AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Autowired
    protected MediaV1RestControllerTest(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/storage/media";
    }

    @Override
    @Test
    protected void testCreate() {
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        var fileReferenceId = UUID.randomUUID();
        var priority = 3;

        CreateMediaRequest request = new CreateMediaRequest();
        request.setFileReferenceId(fileReferenceId);
        request.setPriority(priority);
        request.setSources(
                sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );

        Mockito.when(attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileReferenceId)).thenReturn(ResponseEntity.ok(
                new AttachmentFileDTO()
        ));

        MediaDTO createdMedia = super.create(request);

        assertAll(() -> {
            assertNotNull(createdMedia);
            assertEquals(priority, createdMedia.getPriority());
            assertEquals(fileReferenceId, createdMedia.getFileReferenceId());
            assertEquals(sources.size(), createdMedia.getSources().size());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        MediaDTO foundMedia = super.findById(media.getId());

        assertAll(() -> {
            assertNotNull(foundMedia);
            assertEquals(media.getId(), foundMedia.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media media2 = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<MediaDTO> medias = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(medias);
            assertEquals(2, medias.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );

        var fileReferenceId = UUID.randomUUID();
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(fileReferenceId).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        var priority = 16;

        PatchMediaRequest request = new PatchMediaRequest();
        request.setPriority(JsonNullable.of(16));
        request.setSources(JsonNullable.of(sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()));
        request.setFileReferenceId(JsonNullable.of(fileReferenceId));

        Mockito.when(attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileReferenceId)).thenReturn(ResponseEntity.ok(
                new AttachmentFileDTO()
        ));

        MediaDTO updatedMedia = super.patch(media.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedMedia);
            assertEquals(media.getId(), updatedMedia.getId());
            assertEquals(sources.size(), updatedMedia.getSources().size());
            assertEquals(priority, updatedMedia.getPriority());
            assertEquals(fileReferenceId, updatedMedia.getFileReferenceId());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(media.getId()));
    }
}
