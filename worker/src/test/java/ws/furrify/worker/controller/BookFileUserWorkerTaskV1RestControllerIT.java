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
package ws.furrify.worker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.BookDTO;
import ws.furrify.worker.domain.worker.WorkStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import ws.furrify.worker.WorkerApplication;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTaskRepository;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.CreateBookFileUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;

import java.time.ZonedDateTime;
import java.util.List;
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

    @MockitoBean
    private BookV1RestControllerApiClient bookV1RestControllerApiClient;

    @MockitoBean
    private ws.furrify.core.service.EurekaDiscoveryService eurekaDiscoveryService;

    @Autowired
    protected BookFileUserWorkerTaskV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/workers/user/books/files/generator";
    }

    @BeforeEach
    void setUp() {
        when(attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new AttachmentFileDTO()));
        when(eurekaDiscoveryService.isServiceOnline(any())).thenReturn(true);
    }

    @Override
    @Test
    protected void testCreate() {
        CreateBookFileUserWorkerTaskRequest request = new CreateBookFileUserWorkerTaskRequest();
        request.setSourceBookReferenceId(UUID.randomUUID());
        request.setStartAt(ZonedDateTime.now());
        
        when(bookV1RestControllerApiClient.bookV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new BookDTO()));

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

                        .status(WorkStatus.NOT_STARTED)
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

                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(WorkStatus.NOT_STARTED)
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

                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        
        PatchBookFileUserWorkerTaskRequest patch = new PatchBookFileUserWorkerTaskRequest();
        
        patch.setStartAt(JsonNullable.of(ZonedDateTime.now()));
        assertDoesNotThrow(() -> super.patch(task.getId(), patch));
    }

    @Override
    @Test
    protected void testDelete() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(WorkStatus.NOT_STARTED)
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
    void testCancel() {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

        given()
                .header("Content-Type", "application/json")
                .pathParam("id", task.getId())
                .when()
                .post(this.basePath + "/{id}/cancel")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());

        BookFileUserWorkerTask updatedTask = bookFileUserWorkerTaskRepository.findById(task.getId()).get();
        assertEquals(WorkStatus.CANCELLED, updatedTask.getStatus());
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        CreateBookFileUserWorkerTaskRequest request1 = new CreateBookFileUserWorkerTaskRequest();
        request1.setSourceBookReferenceId(UUID.randomUUID());
        request1.setStartAt(ZonedDateTime.now());

        CreateBookFileUserWorkerTaskRequest request2 = new CreateBookFileUserWorkerTaskRequest();
        request2.setSourceBookReferenceId(UUID.randomUUID());
        request2.setStartAt(ZonedDateTime.now());
        
        when(bookV1RestControllerApiClient.bookV1RestControllerGetById(any())).thenReturn(ResponseEntity.ok(new BookDTO()));

        List<BookFileUserWorkerTaskDTO> createdTasks = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdTasks);
            assertEquals(2, createdTasks.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        BookFileUserWorkerTask task1 = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        BookFileUserWorkerTask task2 = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())
                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );
        
        PatchBookFileUserWorkerTaskRequest patch1 = new PatchBookFileUserWorkerTaskRequest();
        patch1.setStartAt(JsonNullable.of(ZonedDateTime.now()));

        PatchBookFileUserWorkerTaskRequest patch2 = new PatchBookFileUserWorkerTaskRequest();
        patch2.setStartAt(JsonNullable.of(ZonedDateTime.now()));

        List<BookFileUserWorkerTaskDTO> updatedTasks = super.patchBulk(java.util.Map.of(task1.getId(), patch1, task2.getId(), patch2));

        assertAll(() -> {
            assertNotNull(updatedTasks);
            assertEquals(2, updatedTasks.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        BookFileUserWorkerTask task = bookFileUserWorkerTaskRepository.save(
                BookFileUserWorkerTask.builder()
                        .sourceBookReferenceId(UUID.randomUUID())

                        .status(WorkStatus.NOT_STARTED)
                        .startAt(ZonedDateTime.now())
                        .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                        .build()
        );

    BookFileUserWorkerTask task2 = bookFileUserWorkerTaskRepository.save(
            BookFileUserWorkerTask.builder()
                    .sourceBookReferenceId(UUID.randomUUID())

                    .status(WorkStatus.NOT_STARTED)
                    .startAt(ZonedDateTime.now())
                    .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID)
                    .build()
    );

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(task.getId(), task2.getId())));
    }
}
