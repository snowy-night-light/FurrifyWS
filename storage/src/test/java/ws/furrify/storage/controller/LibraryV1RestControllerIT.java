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
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.CreateLibraryRequest;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class LibraryV1RestControllerIT extends BaseCrudControllerTest<Library, LibraryDTO, CreateLibraryRequest, PatchLibraryRequest> {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private BookRepository bookRepository;

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

        assertDoesNotThrow(() -> super.delete(library.getId()));
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

        List<LibraryDTO> createdLibraries = super.createBulk(java.util.List.of(request1, request2));

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

        List<LibraryDTO> updatedLibraries = super.patchBulk(java.util.Map.of(library1.getId(), request1, library2.getId(), request2));

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

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(library.getId(), library2.getId())));
    }
}
