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
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.CreateLibraryRequest;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.storage.dto.stats.LibraryStatisticsDto;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;
import io.restassured.RestAssured;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ws.furrify.openapi.gen.worker.api.BookFileUserWorkerTaskV1RestControllerApiClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class LibraryV1RestControllerIT extends BaseCrudControllerTest<Library, LibraryDTO, CreateLibraryRequest, PatchLibraryRequest> {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @MockitoBean
    private BookFileUserWorkerTaskV1RestControllerApiClient bookFileUserWorkerTaskApiClient;

    @Autowired
    protected LibraryV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/libraries";
    }

    @Override
    @Test
    protected void testCreate() {
        CreateLibraryRequest request = new CreateLibraryRequest();
        request.setTitle("Test library");
        request.setLikesEnabled(false);
        request.setDislikesEnabled(false);

        LibraryDTO createdLibrary = super.create(request);

        assertAll(() -> {
            assertNotNull(createdLibrary);
            assertEquals("Test library", createdLibrary.getTitle());
            assertEquals(false, createdLibrary.getLikesEnabled());
            assertEquals(false, createdLibrary.getDislikesEnabled());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        LibraryDTO foundLibrary = super.findById(library.getId());

        assertAll(() -> {
            assertNotNull(foundLibrary);
            assertEquals(library.getId(), foundLibrary.getId());
            assertEquals("Test library", foundLibrary.getTitle());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        libraryRepository.save(Library.builder().title("Test library 2").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<LibraryDTO> libraries = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(libraries);
            assertTrue(libraries.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchLibraryRequest request = new PatchLibraryRequest();
        request.setTitle(JsonNullable.of("Patched title"));
        request.setLikesEnabled(JsonNullable.of(true));
        request.setDislikesEnabled(JsonNullable.of(true));

        LibraryDTO updatedLibrary = super.patch(library.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedLibrary);
            assertEquals(library.getId(), updatedLibrary.getId());
            assertEquals("Patched title", updatedLibrary.getTitle());
            assertEquals(true, updatedLibrary.getLikesEnabled());
            assertEquals(true, updatedLibrary.getDislikesEnabled());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        // accepted at once, removed in the background
        RestAssured.given().pathParam("id", library.getId())
                .when().delete(this.basePath + "/{id}")
                .then().log().all().statusCode(HttpStatus.ACCEPTED.value());
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> !libraryRepository.existsById(library.getId()));
    }

    @Test
    protected void testLibraryBeingDeletedIsShownAndNotClearedAgain() {
        Library library = libraryRepository.save(Library.builder().title("Test library").scheduledForRemoval(true)
                .removalHeartbeat(ZonedDateTime.now()) // a running deletion's lease: not taken over meanwhile
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertEquals(Boolean.TRUE, findById(library.getId()).getScheduledForRemoval(), "clients see it is being removed");
        assertEquals(Boolean.TRUE, findById(library.getId()).getRemovalInProgress());
        RestAssured.given().pathParam("id", library.getId()).queryParam("parts", "BOOKS")
                .when().post(this.basePath + "/{id}/clear")
                .then().log().all().statusCode(HttpStatus.CONFLICT.value())
                .body("message", Matchers.containsString("is being deleted"));
    }

    @Test
    protected void testSecondClearIsRefusedWithAReason() {
        Library library = libraryRepository.save(Library.builder().title("Test library").contentRemoval(List.of("BOOKS"))
                .removalHeartbeat(ZonedDateTime.now()) // a running clear's lease: not taken over meanwhile
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertEquals(Boolean.TRUE, findById(library.getId()).getRemovalInProgress(), "clients see content being removed");
        RestAssured.given().pathParam("id", library.getId()).queryParam("parts", "TAGS")
                .when().post(this.basePath + "/{id}/clear")
                .then().log().all().statusCode(HttpStatus.CONFLICT.value())
                .body("message", Matchers.containsString("already being removed (BOOKS)"));
    }

    @Test
    protected void testStatistics() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        for (String title : List.of("Book 1", "Book 2")) {
            bookRepository.save(Book.builder().title(title).descriptionHtml("Desc").shortDescriptionHtml("short").library(library)
                    .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        }
        String name = "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(name).hexColor("#FFFFFF").library(library)
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagRepository.save(Tag.builder().name(name).category(category).library(library)
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        LibraryStatisticsDto statistics = RestAssured.given().pathParam("id", library.getId())
                .when().get(this.basePath + "/{id}/statistics")
                .then().log().all().statusCode(HttpStatus.OK.value())
                .extract().as(LibraryStatisticsDto.class);

        assertAll(
                () -> assertEquals(library.getId(), statistics.getLibraryId()),
                () -> assertEquals(2, statistics.getBooksCount()),
                () -> assertEquals(0, statistics.getChaptersCount()),
                () -> assertEquals(0, statistics.getPostsCount()),
                () -> assertEquals(0, statistics.getCollectionsCount()),
                () -> assertEquals(1, statistics.getTagsCount()),
                () -> assertEquals(1, statistics.getTagCategoriesCount()),
                () -> assertEquals(0, statistics.getArtistsCount())
        );
    }

    @Test
    protected void testStatisticsOfUnknownLibraryIsNotFound() {
        RestAssured.given().pathParam("id", UUID.randomUUID())
                .when().get(this.basePath + "/{id}/statistics")
                .then().log().all().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    protected void testClearingBooksCancelsTheirGenerationTasks() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        UUID taskId = UUID.randomUUID();
        Book book = bookRepository.save(Book.builder().title("Book").descriptionHtml("Desc").shortDescriptionHtml("short").library(library)
                .activeWorkerTaskId(taskId).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        RestAssured.given().pathParam("id", library.getId()).queryParam("parts", "BOOKS")
                .when().post(this.basePath + "/{id}/clear")
                .then().log().all().statusCode(HttpStatus.ACCEPTED.value());
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> !bookRepository.existsById(book.getId()));

        Mockito.verify(bookFileUserWorkerTaskApiClient, Mockito.timeout(5000))
                .bookFileUserWorkerTaskV1RestControllerCancelBulk(List.of(taskId));
    }

    @Test
    protected void testClearKeepsTheLibrary() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        String name = "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(name).hexColor("#FFFFFF").library(library)
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(name).category(category).library(library)
                .ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        RestAssured.given().pathParam("id", library.getId()).queryParam("parts", "TAGS")
                .when().post(this.basePath + "/{id}/clear")
                .then().log().all().statusCode(HttpStatus.ACCEPTED.value());
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> !tagRepository.existsById(tag.getId()) && !tagCategoryRepository.existsById(category.getId()));
        assertTrue(libraryRepository.existsById(library.getId()), "the library stays");
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> findById(library.getId()).getContentRemoval() == null);
        assertEquals(Boolean.FALSE, findById(library.getId()).getRemovalInProgress(), "nothing runs any more");
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        CreateLibraryRequest request1 = new CreateLibraryRequest();
        request1.setTitle("Test library 1");
        request1.setLikesEnabled(false);
        request1.setDislikesEnabled(false);

        CreateLibraryRequest request2 = new CreateLibraryRequest();
        request2.setTitle("Test library 2");
        request2.setLikesEnabled(true);
        request2.setDislikesEnabled(true);

        List<LibraryDTO> createdLibraries = super.createBulk(List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdLibraries);
            assertEquals(2, createdLibraries.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library1 = libraryRepository.save(Library.builder().title("Test library 1").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Library library2 = libraryRepository.save(Library.builder().title("Test library 2").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchLibraryRequest request1 = new PatchLibraryRequest();
        request1.setTitle(JsonNullable.of("Patched title 1"));
        request1.setLikesEnabled(JsonNullable.of(true));
        request1.setDislikesEnabled(JsonNullable.of(true));

        PatchLibraryRequest request2 = new PatchLibraryRequest();
        request2.setTitle(JsonNullable.of("Patched title 2"));
        request2.setLikesEnabled(JsonNullable.of(false));
        request2.setDislikesEnabled(JsonNullable.of(false));

        List<LibraryDTO> updatedLibraries = super.patchBulk(Map.of(library1.getId(), request1, library2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedLibraries);
            assertEquals(2, updatedLibraries.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Library library2 = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        RestAssured.given().header("Content-Type", "application/json")
                .body(List.of(library.getId(), library2.getId()))
                .when().delete(this.basePath + "/bulk")
                .then().log().all().statusCode(HttpStatus.ACCEPTED.value());
        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> !libraryRepository.existsById(library.getId()) && !libraryRepository.existsById(library2.getId()));
    }
}
