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
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.CreateBookRequest;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;
import java.util.UUID;

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
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private ArtistRepository artistRepository;

    private Library defaultLibrary;

    @Autowired
    protected BookV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/books";
    }

    private String randomName() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
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
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Test book");
        request.setDescription("Test description");
        request.setLibrary(EntityIdRequest.builder().id(defaultLibrary.getId()).build());
        request.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request.setArtists(List.of(EntityIdRequest.builder().id(artist.getId()).build()));

        BookDTO createdBook = super.create(request);

        assertAll(() -> {
            assertNotNull(createdBook);
            assertEquals("Test book", createdBook.getTitle());
            assertEquals("Test description", createdBook.getDescription());
            assertNotNull(createdBook.getTags());
            assertEquals(1, createdBook.getTags().size());
            assertEquals(tag.getId(), createdBook.getTags().getFirst().getId());
            assertNotNull(createdBook.getArtists());
            assertEquals(1, createdBook.getArtists().size());
            assertEquals(artist.getId(), createdBook.getArtists().getFirst().getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        setupData();
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book = bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        BookDTO foundBook = super.findById(book.getId());

        assertAll(() -> {
            assertNotNull(foundBook);
            assertEquals(book.getId(), foundBook.getId());
            assertEquals("Test book", foundBook.getTitle());
            assertNotNull(foundBook.getTags());
            assertEquals(1, foundBook.getTags().size());
            assertEquals(tag.getId(), foundBook.getTags().getFirst().getId());
            assertNotNull(foundBook.getArtists());
            assertEquals(1, foundBook.getArtists().size());
            assertEquals(artist.getId(), foundBook.getArtists().getFirst().getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        setupData();
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookRepository.save(Book.builder().title("Test book 2").description("Desc").library(defaultLibrary).tags(List.of(tag2)).artists(List.of(artist2)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book = bookRepository.save(Book.builder().title("Test book").description("Desc").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookRequest request = new PatchBookRequest();
        request.setTitle(JsonNullable.of("Patched title"));
        request.setTags(JsonNullable.of(List.of(EntityIdRequest.builder().id(tag2.getId()).build())));
        request.setArtists(JsonNullable.of(List.of(EntityIdRequest.builder().id(artist2.getId()).build())));

        BookDTO updatedBook = super.patch(book.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedBook);
            assertEquals(book.getId(), updatedBook.getId());
            assertEquals("Patched title", updatedBook.getTitle());
            assertNotNull(updatedBook.getTags());
            assertEquals(1, updatedBook.getTags().size());
            assertEquals(tag2.getId(), updatedBook.getTags().getFirst().getId());
            assertNotNull(updatedBook.getArtists());
            assertEquals(1, updatedBook.getArtists().size());
            assertEquals(artist2.getId(), updatedBook.getArtists().getFirst().getId());
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
