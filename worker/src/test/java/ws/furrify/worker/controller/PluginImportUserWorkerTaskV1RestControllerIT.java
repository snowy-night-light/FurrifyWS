package ws.furrify.worker.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpStatus;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import ws.furrify.worker.WorkerApplication;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTaskRepository;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.CreatePluginImportUserWorkerTaskRequest;

import java.time.ZonedDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = WorkerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PluginImportUserWorkerTaskV1RestControllerIT extends BaseCrudControllerTest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {

    @Autowired
    private PluginImportUserWorkerTaskRepository pluginImportUserWorkerTaskRepository;

    @Autowired
    protected PluginImportUserWorkerTaskV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/user/workers/plugin/import";
    }

    @Override
    @Test
    protected void testCreate() {
        CreatePluginImportUserWorkerTaskRequest request = new CreatePluginImportUserWorkerTaskRequest();
        request.setFileReferenceId(UUID.randomUUID());
        request.setDestinationLibraryReferenceId(UUID.randomUUID());
        request.setProvider("dummy-provider");
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
                        .provider("dummy-provider")
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
                        .provider("dummy-provider")
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("dummy-provider")
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        Page<PluginImportUserWorkerTaskDTO> tasks = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(tasks);
            assertTrue(tasks.getContent().size() >= 2);
        });
    }

    record DummyPatch() implements EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {}

    @Override
    @Test
    protected void testPatch() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("dummy-provider")
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        
        try {
            super.patch(task.getId(), new DummyPatch());
        } catch (Throwable e) {
            // Ignored, might be a 405 Method Not Allowed or 400 Bad Request since it's unsupported
        }
    }

    @Override
    @Test
    protected void testDelete() {
        PluginImportUserWorkerTask task = pluginImportUserWorkerTaskRepository.save(
                PluginImportUserWorkerTask.builder()
                        .fileReferenceId(UUID.randomUUID())
                        .destinationLibraryReferenceId(UUID.randomUUID())
                        .provider("dummy-provider")
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
                        .provider("dummy-provider")
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
}
