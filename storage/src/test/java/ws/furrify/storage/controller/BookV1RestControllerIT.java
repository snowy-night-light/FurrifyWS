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
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.CreateBookRequest;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class BookV1RestControllerIT extends BaseCrudControllerTest<Book, BookDTO, CreateBookRequest, PatchBookRequest> {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    private Library defaultLibrary;

    @Autowired
    protected BookV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/books";
    }

    private void setupData() {
        if (defaultLibrary == null) {
            defaultLibrary = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        }
    }

    @Override
    @Test
    protected void testCreate() {
        setupData();

        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Test book");
        request.setDescription("Test description");
        request.setLibrary(EntityIdRequest.builder().id(defaultLibrary.getId()).build());

        BookDTO createdBook = super.create(request);

        assertAll(() -> {
            assertNotNull(createdBook);
            assertEquals("Test book", createdBook.getTitle());
            assertEquals("Test description", createdBook.getDescription());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        BookDTO foundBook = super.findById(book.getId());

        assertAll(() -> {
            assertNotNull(foundBook);
            assertEquals(book.getId(), foundBook.getId());
            assertEquals("Test book", foundBook.getTitle());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        setupData();
        bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookRepository.save(Book.builder().title("Test book 2").description("Desc").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<BookDTO> books = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(books);
            assertTrue(books.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookRequest request = new PatchBookRequest();
        request.setTitle(JsonNullable.of("Patched title"));

        BookDTO updatedBook = super.patch(book.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedBook);
            assertEquals(book.getId(), updatedBook.getId());
            assertEquals("Patched title", updatedBook.getTitle());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(book.getId()));
    }
}
