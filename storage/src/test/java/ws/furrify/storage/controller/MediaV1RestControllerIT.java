/*
 * furrify-storage-service - Furrify Workspace Project
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MediaV1RestControllerIT extends BaseCrudControllerTest<Media, MediaDTO, CreateMediaRequest, PatchMediaRequest> {

    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private SourceRepository sourceRepository;

    @MockitoBean
    private AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Autowired
    protected MediaV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/media";
    }

    @Override
    @Test
    protected void testCreate() {
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        var fileReferenceId = UUID.randomUUID();
        var priority = 3;

        CreateMediaRequest request = new CreateMediaRequest();
        request.setFileReferenceId(fileReferenceId);
        request.setPriority(priority);
        request.setSources(
                sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );

        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId);

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
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );

        var fileReferenceId = UUID.randomUUID();
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(fileReferenceId).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        var priority = 16;

        PatchMediaRequest request = new PatchMediaRequest();
        request.setPriority(JsonNullable.of(16));
        request.setSources(JsonNullable.of(sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()));
        request.setFileReferenceId(JsonNullable.of(fileReferenceId));

        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId);

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

        Mockito.verify(attachmentFileV1RestControllerApiClient, Mockito.times(1))
                .attachmentFileV1RestControllerDelete(media.getId());
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        List<Source> sources1 = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        List<Source> sources2 = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        var fileReferenceId1 = UUID.randomUUID();
        var fileReferenceId2 = UUID.randomUUID();

        CreateMediaRequest request1 = new CreateMediaRequest();
        request1.setFileReferenceId(fileReferenceId1);
        request1.setPriority(3);
        request1.setSources(
                sources1.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );

        CreateMediaRequest request2 = new CreateMediaRequest();
        request2.setFileReferenceId(fileReferenceId2);
        request2.setPriority(4);
        request2.setSources(
                sources2.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );

        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId1);
        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId2);

        List<MediaDTO> createdMedia = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdMedia);
            assertEquals(2, createdMedia.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        List<Source> sources1 = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        List<Source> sources2 = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );

        var fileReferenceId1 = UUID.randomUUID();
        var fileReferenceId2 = UUID.randomUUID();
        Media media1 = mediaRepository.save(Media.builder().priority(1).fileReferenceId(fileReferenceId1).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media media2 = mediaRepository.save(Media.builder().priority(2).fileReferenceId(fileReferenceId2).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchMediaRequest request1 = new PatchMediaRequest();
        request1.setPriority(JsonNullable.of(16));
        request1.setSources(JsonNullable.of(sources1.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()));
        request1.setFileReferenceId(JsonNullable.of(fileReferenceId1));

        PatchMediaRequest request2 = new PatchMediaRequest();
        request2.setPriority(JsonNullable.of(17));
        request2.setSources(JsonNullable.of(sources2.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()));
        request2.setFileReferenceId(JsonNullable.of(fileReferenceId2));

        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId1);
        Mockito.doReturn(ResponseEntity.ok(new AttachmentFileDTO()))
                .when(attachmentFileV1RestControllerApiClient).attachmentFileV1RestControllerGetById(fileReferenceId2);

        List<MediaDTO> updatedMedia = super.patchBulk(java.util.Map.of(media1.getId(), request1, media2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedMedia);
            assertEquals(2, updatedMedia.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Media media = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media media2 = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(media.getId(), media2.getId())));
    }
}
