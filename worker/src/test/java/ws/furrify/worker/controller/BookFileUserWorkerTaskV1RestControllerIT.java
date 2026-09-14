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
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import ws.furrify.worker.WorkerApplication;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTaskRepository;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.CreateBookFileUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;

import java.time.ZonedDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = WorkerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class BookFileUserWorkerTaskV1RestControllerIT extends BaseCrudControllerTest<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest, PatchBookFileUserWorkerTaskRequest> {

    @Autowired
    private BookFileUserWorkerTaskRepository bookFileUserWorkerTaskRepository;

    @MockitoBean
    private AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Autowired
    protected BookFileUserWorkerTaskV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/user/workers/books/files/generator";
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        when(attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new org.openapitools.model.AttachmentFileDTO()));
    }

    @Override
    @Test
    protected void testCreate() {
        CreateBookFileUserWorkerTaskRequest request = new CreateBookFileUserWorkerTaskRequest();
        request.setSourceBookReferenceId(UUID.randomUUID());
        request.setStartAt(ZonedDateTime.now());

        BookFileUserWorkerTaskDTO createdTask = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTask);
            assertEquals(request.getSourceBookReferenceId(), createdTask.getSourceBookReferenceId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        BookFileUserWorkerTaskDTO foundTask = super.findById(task.getId());

        assertAll(() -> {
            assertNotNull(foundTask);
            assertEquals(task.getId(), foundTask.getId());
            assertEquals(task.getSourceBookReferenceId(), foundTask.getSourceBookReferenceId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        Page<BookFileUserWorkerTaskDTO> tasks = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(tasks);
            assertTrue(tasks.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        
        PatchBookFileUserWorkerTaskRequest patch = new PatchBookFileUserWorkerTaskRequest();
        
        assertDoesNotThrow(() -> super.patch(task.getId(), patch));
    }

    @Override
    @Test
    protected void testDelete() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        assertDoesNotThrow(() -> super.delete(task.getId()));
    }

    @Test
    void testTriggerExecution() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())
                        .status(ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED)
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
}
