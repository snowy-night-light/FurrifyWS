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
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.book.chapter.BookChapterRepository;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersionRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.CreateBookChapterVersionRequest;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import java.util.List;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookChapterVersionV1RestControllerIT extends BaseCrudControllerTest<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest, PatchBookChapterVersionRequest> {

    @Autowired
    private BookChapterVersionRepository bookChapterVersionRepository;
    @Autowired
    private BookChapterRepository bookChapterRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    private BookChapter defaultChapter;

    @Autowired
    protected BookChapterVersionV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/books/chapters/versions";
    }

    private void setupData() {
        if (defaultChapter == null) {
            Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
            Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(library).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
            defaultChapter = bookChapterRepository.save(BookChapter.builder().chapterNumber(1).title("Test chapter").book(book).versions(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        }
    }

    @Override
    @Test
    protected void testCreate() {
        setupData();

        CreateBookChapterVersionRequest request = new CreateBookChapterVersionRequest();
        request.setContentHtml("Test contentHtml");
        request.setChapter(EntityIdRequest.builder().id(defaultChapter.getId()).build());
        request.setAuthorNotesStart("Start");
        request.setAuthorNotesEnd("End");
        request.setContentUpdatedAt(ZonedDateTime.now());

        BookChapterVersionDTO createdVersion = super.create(request);

        assertAll(() -> {
            assertNotNull(createdVersion);
            assertEquals("Test contentHtml", createdVersion.getContentHtml());
            assertEquals(defaultChapter.getId(), createdVersion.getChapter().getId());
            assertEquals("Start", createdVersion.getAuthorNotesStart());
            assertEquals("End", createdVersion.getAuthorNotesEnd());
            assertNotNull(createdVersion.getContentUpdatedAt());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        setupData();
        BookChapterVersion version = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        BookChapterVersionDTO foundVersion = super.findById(version.getId());

        assertAll(() -> {
            assertNotNull(foundVersion);
            assertEquals(version.getId(), foundVersion.getId());
            assertEquals("Test contentHtml", foundVersion.getContentHtml());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        setupData();
        bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(2).contentHtml("Test contentHtml 2").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<BookChapterVersionDTO> versions = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(versions);
            assertTrue(versions.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        setupData();
        BookChapterVersion version = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookChapterVersionRequest request = new PatchBookChapterVersionRequest();
        request.setContentHtml(JsonNullable.of("Patched contentHtml"));
        request.setAuthorNotesStart(JsonNullable.of("Patched Start"));
        request.setAuthorNotesEnd(JsonNullable.of("Patched End"));
        ZonedDateTime now = ZonedDateTime.now();
        request.setContentUpdatedAt(JsonNullable.of(now));

        BookChapterVersionDTO updatedVersion = super.patch(version.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedVersion);
            assertEquals(version.getId(), updatedVersion.getId());
            assertEquals("Patched contentHtml", updatedVersion.getContentHtml());
            assertEquals("Patched Start", updatedVersion.getAuthorNotesStart());
            assertEquals("Patched End", updatedVersion.getAuthorNotesEnd());
            assertNotNull(updatedVersion.getContentUpdatedAt());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        setupData();
        BookChapterVersion version = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml").authorNotesStart("Start").authorNotesEnd("End").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(version.getId()));
    }
}
