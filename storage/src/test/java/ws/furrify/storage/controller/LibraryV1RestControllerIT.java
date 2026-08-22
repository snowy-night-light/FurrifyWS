package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.CreateLibraryRequest;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class LibraryV1RestControllerIT extends BaseCrudControllerTest<Library, LibraryDTO, CreateLibraryRequest, PatchLibraryRequest> {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    protected LibraryV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/storage/libraries";
    }

    @Override
    @Test
    protected void testCreate() {
        CreateLibraryRequest request = new CreateLibraryRequest();
        request.setTitle("Test library");

        LibraryDTO createdLibrary = super.create(request);

        assertAll(() -> {
            assertNotNull(createdLibrary);
            assertEquals("Test library", createdLibrary.getTitle());
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

        LibraryDTO updatedLibrary = super.patch(library.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedLibrary);
            assertEquals(library.getId(), updatedLibrary.getId());
            assertEquals("Patched title", updatedLibrary.getTitle());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(library.getId()));
    }
}
