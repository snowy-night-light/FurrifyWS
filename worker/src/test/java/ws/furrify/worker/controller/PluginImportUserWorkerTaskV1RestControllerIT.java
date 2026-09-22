package ws.furrify.worker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.LibraryV1RestControllerApiClient;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import ws.furrify.worker.WorkerApplication;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTaskRepository;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.CreatePluginImportUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.plugin.request.PatchPluginImportUserWorkerTaskRequest;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.LibraryDTO;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.model.WorkerPluginResults;
import ws.furrify.worker.shared.plugin.ImportV1WorkerPluginIntf;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = WorkerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PluginImportUserWorkerTaskV1RestControllerIT extends BaseCrudControllerTest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest, PatchPluginImportUserWorkerTaskRequest> {

    @Autowired
    private PluginImportUserWorkerTaskRepository pluginImportUserWorkerTaskRepository;

    @MockitoBean
    private AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @MockitoBean
    private LibraryV1RestControllerApiClient libraryV1RestControllerApiClient;

    @MockitoBean
    private ExternalPluginLoaderService externalPluginLoaderService;

    @Autowired
    protected PluginImportUserWorkerTaskV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/workers/user/plugin/import";
    }

    public static class DummyPlugin implements ImportV1WorkerPluginIntf {
        @Override
        public boolean validateSchema(File file) { return true; }
        @Override
        public WorkerPluginResults loadSchemaDataIntoLibrary(File file, UUID libraryId) { return null; }
        @Override
        public String[] getAllowedExtensions() { return new String[]{}; }
        @Override
        public String getProviderName() { return "dummy-provider"; }
        @Override
        public WorkerPluginResults trackCurrentStatus() { return null; }
    }

    @BeforeEach
    void setUp() {
        when(attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new AttachmentFileDTO()));
        when(libraryV1RestControllerApiClient.libraryV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new LibraryDTO()));
        ImportV1WorkerPluginIntf mockPlugin = new DummyPlugin();
        when(externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class)).thenReturn(List.of(mockPlugin));
    }

    @Override
    @Test
    protected void testCreate() {
        CreatePluginImportUserWorkerTaskRequest request = new CreatePluginImportUserWorkerTaskRequest();
        request.setFileReferenceId(UUID.randomUUID());
        request.setDestinationLibraryReferenceId(UUID.randomUUID());
        request.setProvider("DummyPlugin");
        request.setStartAt(ZonedDateTime.now());

        PluginImportUserWorkerTaskDTO createdTask = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTask);
            assertEquals(request.getFileReferenceId(), createdTask.getFileReferenceId());
            assertEquals(request.getDestinationLibraryReferenceId(), createdTask.getDestinationLibraryReferenceId());
            assertEquals(request.getProvider(), createdTask.getProvider());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        PluginImportUserWorkerTaskDTO foundTask = super.findById(task.getId());

        assertAll(() -> {
            assertNotNull(foundTask);
            assertEquals(task.getId(), foundTask.getId());
            assertEquals(task.getFileReferenceId(), foundTask.getFileReferenceId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        Page<PluginImportUserWorkerTaskDTO> tasks = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(tasks);
            assertTrue(tasks.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        
        PatchPluginImportUserWorkerTaskRequest patch = new PatchPluginImportUserWorkerTaskRequest();
        
        assertDoesNotThrow(() -> super.patch(task.getId(), patch));
    }

    @Override
    @Test
    protected void testDelete() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        assertDoesNotThrow(() -> super.delete(task.getId()));
    }

    @Test
    void testTriggerExecution() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("DummyPlugin")
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        given()
                .header("Content-Type", "application/json")
                .pathParam("id", task.getId())
                .when()
                .post(this.basePath + "/{id}/execute")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetInstalledPlugins() {
        given()
                .header("Content-Type", "application/json")
                .when()
                .get(this.basePath + "/list")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
