package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.book.chapter.BookChapterRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.request.CreateBookChapterRequest;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class BookChapterV1RestControllerIT extends BaseCrudControllerTest<BookChapter, BookChapterDTO, CreateBookChapterRequest, PatchBookChapterRequest> {

    @Autowired
    private BookChapterRepository bookChapterRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    private Book defaultBook;

    @Autowired
    protected BookChapterV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/books/chapters";
    }

    private void setupData() {
        if (defaultBook == null) {
            Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
            defaultBook = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(library).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        }
    }

    @Override
    @Test
    protected void testCreate() {
        setupData();

        CreateBookChapterRequest request = new CreateBookChapterRequest();
        request.setTitle("Test chapter");
        request.setBook(EntityIdRequest.builder().id(defaultBook.getId()).build());
        request.setViews(0L);
        request.setChapterNumber(1);

        BookChapterDTO createdChapter = super.create(request);

        assertAll(() -> {
            assertNotNull(createdChapter);
            assertEquals("Test chapter", createdChapter.getTitle());
            assertEquals(defaultBook.getId(), createdChapter.getBook().getId());
            assertEquals(0L, createdChapter.getViews());
            assertEquals(1, createdChapter.getChapterNumber());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        setupData();
        BookChapter chapter = bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter").book(defaultBook).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        BookChapterDTO foundChapter = super.findById(chapter.getId());

        assertAll(() -> {
            assertNotNull(foundChapter);
            assertEquals(chapter.getId(), foundChapter.getId());
            assertEquals("Test chapter", foundChapter.getTitle());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        setupData();
        bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter").book(defaultBook).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter 2").book(defaultBook).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<BookChapterDTO> chapters = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(chapters);
            assertTrue(chapters.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        setupData();
        BookChapter chapter = bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter").book(defaultBook).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookChapterRequest request = new PatchBookChapterRequest();
        request.setTitle(JsonNullable.of("Patched title"));
        request.setViews(JsonNullable.of(25L));
        request.setChapterNumber(JsonNullable.of(2));

        BookChapterDTO updatedChapter = super.patch(chapter.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedChapter);
            assertEquals(chapter.getId(), updatedChapter.getId());
            assertEquals("Patched title", updatedChapter.getTitle());
            assertEquals(25L, updatedChapter.getViews());
            assertEquals(2, updatedChapter.getChapterNumber());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        setupData();
        BookChapter chapter = bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter").book(defaultBook).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(chapter.getId()));
    }
}
