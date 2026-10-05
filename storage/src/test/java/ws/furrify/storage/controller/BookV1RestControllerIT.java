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
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.BookRating;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.book.BookStatus;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.CreateBookRequest;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.dto.book.request.PutBookWorkerTaskRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;
import io.restassured.RestAssured;

import java.util.List;
import java.util.Map;
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

        Book seqBook = bookRepository.save(Book.builder().title("Sequel").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book preBook = bookRepository.save(Book.builder().title("Prequel").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Test book");
        request.setDescriptionHtml("Test descriptionHtml");
        request.setLibrary(EntityIdRequest.builder().id(defaultLibrary.getId()).build());
        request.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request.setArtists(List.of(EntityIdRequest.builder().id(artist.getId()).build()));
        request.setSequels(List.of(EntityIdRequest.builder().id(seqBook.getId()).build()));
        request.setPrequels(List.of(EntityIdRequest.builder().id(preBook.getId()).build()));
        request.setShortDescriptionHtml("Short desc");
        request.setStatus(BookStatus.COMPLETED);
        request.setRating(BookRating.SAFE);
        request.setViews(10L);

        BookDTO createdBook = super.create(request);

        assertAll(() -> {
            assertNotNull(createdBook);
            assertEquals("Test book", createdBook.getTitle());
            assertEquals("Test descriptionHtml", createdBook.getDescriptionHtml());
            assertEquals("Short desc", createdBook.getShortDescriptionHtml());
            assertEquals(BookStatus.COMPLETED, createdBook.getStatus());
            assertEquals(BookRating.SAFE, createdBook.getRating());
            assertEquals(10L, createdBook.getViews());
            assertNotNull(createdBook.getTags());
            assertEquals(1, createdBook.getTags().size());
            assertEquals(tag.getId(), createdBook.getTags().getFirst().getId());
            assertNotNull(createdBook.getArtists());
            assertEquals(1, createdBook.getArtists().size());
            assertEquals(artist.getId(), createdBook.getArtists().getFirst().getId());
            assertNotNull(createdBook.getSequels());
            assertEquals(1, createdBook.getSequels().size());
            assertEquals(seqBook.getId(), createdBook.getSequels().getFirst());
            assertNotNull(createdBook.getPrequels());
            assertEquals(1, createdBook.getPrequels().size());
            assertEquals(preBook.getId(), createdBook.getPrequels().getFirst());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        setupData();
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        bookRepository.save(Book.builder().title("Test book 2").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).tags(List.of(tag2)).artists(List.of(artist2)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Book seqBook2 = bookRepository.save(Book.builder().title("Sequel2").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book preBook2 = bookRepository.save(Book.builder().title("Prequel2").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookRequest request = new PatchBookRequest();
        request.setTitle(JsonNullable.of("Patched title"));
        request.setTags(JsonNullable.of(List.of(EntityIdRequest.builder().id(tag2.getId()).build())));
        request.setArtists(JsonNullable.of(List.of(EntityIdRequest.builder().id(artist2.getId()).build())));
        request.setSequels(JsonNullable.of(List.of(EntityIdRequest.builder().id(seqBook2.getId()).build())));
        request.setPrequels(JsonNullable.of(List.of(EntityIdRequest.builder().id(preBook2.getId()).build())));
        request.setShortDescriptionHtml(JsonNullable.of("Patched short desc"));
        request.setStatus(JsonNullable.of(BookStatus.IN_PROGRESS));
        request.setRating(JsonNullable.of(BookRating.TEEN));
        request.setViews(JsonNullable.of(20L));

        BookDTO updatedBook = super.patch(book.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedBook);
            assertEquals(book.getId(), updatedBook.getId());
            assertEquals("Patched title", updatedBook.getTitle());
            assertEquals("Patched short desc", updatedBook.getShortDescriptionHtml());
            assertEquals(BookStatus.IN_PROGRESS, updatedBook.getStatus());
            assertEquals(BookRating.TEEN, updatedBook.getRating());
            assertEquals(20L, updatedBook.getViews());
            assertNotNull(updatedBook.getTags());
            assertEquals(1, updatedBook.getTags().size());
            assertEquals(tag2.getId(), updatedBook.getTags().getFirst().getId());
            assertNotNull(updatedBook.getArtists());
            assertEquals(1, updatedBook.getArtists().size());
            assertEquals(artist2.getId(), updatedBook.getArtists().getFirst().getId());
            assertNotNull(updatedBook.getSequels());
            assertEquals(1, updatedBook.getSequels().size());
            assertEquals(seqBook2.getId(), updatedBook.getSequels().getFirst());
            assertNotNull(updatedBook.getPrequels());
            assertEquals(1, updatedBook.getPrequels().size());
            assertEquals(preBook2.getId(), updatedBook.getPrequels().getFirst());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(book.getId()));
    }

    @Test
    void testUpdateWorkerTaskInfo() {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PutBookWorkerTaskRequest request = new PutBookWorkerTaskRequest();
        request.setFormatReferenceIds(Map.of("html", UUID.randomUUID()));
        request.setActiveWorkerTaskId(UUID.randomUUID());

        RestAssured.given()
                .header("Content-Type", "application/json")
                .pathParam("id", book.getId())
                .body(request)
                .when()
                .put(super.basePath + "/{id}/files/worker-task")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(request.getActiveWorkerTaskId(), updatedBook.getActiveWorkerTaskId());
        assertEquals(request.getFormatReferenceIds(), updatedBook.getFormatReferenceIds());
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        setupData();
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Book seqBook = bookRepository.save(Book.builder().title("Sequel").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book preBook = bookRepository.save(Book.builder().title("Prequel").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateBookRequest request1 = new CreateBookRequest();
        request1.setTitle("Test book 1");
        request1.setDescriptionHtml("Test descriptionHtml 1");
        request1.setLibrary(EntityIdRequest.builder().id(defaultLibrary.getId()).build());
        request1.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request1.setArtists(List.of(EntityIdRequest.builder().id(artist.getId()).build()));
        request1.setSequels(List.of(EntityIdRequest.builder().id(seqBook.getId()).build()));
        request1.setPrequels(List.of(EntityIdRequest.builder().id(preBook.getId()).build()));
        request1.setShortDescriptionHtml("Short desc 1");
        request1.setStatus(BookStatus.COMPLETED);
        request1.setRating(BookRating.SAFE);
        request1.setViews(10L);

        CreateBookRequest request2 = new CreateBookRequest();
        request2.setTitle("Test book 2");
        request2.setDescriptionHtml("Test descriptionHtml 2");
        request2.setLibrary(EntityIdRequest.builder().id(defaultLibrary.getId()).build());
        request2.setShortDescriptionHtml("Short desc 2");
        request2.setStatus(BookStatus.IN_PROGRESS);
        request2.setRating(BookRating.SAFE);
        request2.setViews(5L);

        List<BookDTO> createdBooks = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdBooks);
            assertEquals(2, createdBooks.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        setupData();
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(randomName()).category(category).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book1 = bookRepository.save(Book.builder().title("Test book 1").descriptionHtml("Desc 1").shortDescriptionHtml("short 1").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book2 = bookRepository.save(Book.builder().title("Test book 2").descriptionHtml("Desc 2").shortDescriptionHtml("short 2").library(defaultLibrary).tags(List.of(tag)).artists(List.of(artist)).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Book seqBook2 = bookRepository.save(Book.builder().title("Sequel2").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book preBook2 = bookRepository.save(Book.builder().title("Prequel2").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookRequest request1 = new PatchBookRequest();
        request1.setTitle(JsonNullable.of("Patched title 1"));
        request1.setTags(JsonNullable.of(List.of(EntityIdRequest.builder().id(tag2.getId()).build())));
        request1.setArtists(JsonNullable.of(List.of(EntityIdRequest.builder().id(artist2.getId()).build())));
        request1.setSequels(JsonNullable.of(List.of(EntityIdRequest.builder().id(seqBook2.getId()).build())));
        request1.setPrequels(JsonNullable.of(List.of(EntityIdRequest.builder().id(preBook2.getId()).build())));
        request1.setShortDescriptionHtml(JsonNullable.of("Patched short desc 1"));
        request1.setStatus(JsonNullable.of(BookStatus.IN_PROGRESS));
        request1.setRating(JsonNullable.of(BookRating.TEEN));
        request1.setViews(JsonNullable.of(20L));

        PatchBookRequest request2 = new PatchBookRequest();
        request2.setTitle(JsonNullable.of("Patched title 2"));
        request2.setShortDescriptionHtml(JsonNullable.of("Patched short desc 2"));
        request2.setViews(JsonNullable.of(30L));

        List<BookDTO> updatedBooks = super.patchBulk(java.util.Map.of(book1.getId(), request1, book2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedBooks);
            assertEquals(2, updatedBooks.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        setupData();
        Book book = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Book book2 = bookRepository.save(Book.builder().title("Test book").descriptionHtml("Desc").shortDescriptionHtml("short").library(defaultLibrary).chapters(List.of()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(book.getId(), book2.getId())));
    }
}
