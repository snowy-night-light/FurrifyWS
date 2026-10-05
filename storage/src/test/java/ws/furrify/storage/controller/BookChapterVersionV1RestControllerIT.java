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


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        setupData();

        CreateBookChapterVersionRequest request1 = new CreateBookChapterVersionRequest();
        request1.setContentHtml("Test contentHtml 1");
        request1.setChapter(EntityIdRequest.builder().id(defaultChapter.getId()).build());
        request1.setAuthorNotesStart("Start 1");
        request1.setAuthorNotesEnd("End 1");
        request1.setContentUpdatedAt(ZonedDateTime.now());

        CreateBookChapterVersionRequest request2 = new CreateBookChapterVersionRequest();
        request2.setContentHtml("Test contentHtml 2");
        request2.setChapter(EntityIdRequest.builder().id(defaultChapter.getId()).build());
        request2.setAuthorNotesStart("Start 2");
        request2.setAuthorNotesEnd("End 2");
        request2.setContentUpdatedAt(ZonedDateTime.now());

        List<BookChapterVersionDTO> createdVersions = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdVersions);
            assertEquals(2, createdVersions.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        setupData();
        BookChapterVersion version1 = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml 1").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        BookChapterVersion version2 = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(2).contentHtml("Test contentHtml 2").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchBookChapterVersionRequest request1 = new PatchBookChapterVersionRequest();
        request1.setContentHtml(JsonNullable.of("Patched contentHtml 1"));
        request1.setAuthorNotesStart(JsonNullable.of("Patched Start 1"));
        request1.setAuthorNotesEnd(JsonNullable.of("Patched End 1"));
        request1.setContentUpdatedAt(JsonNullable.of(ZonedDateTime.now()));

        PatchBookChapterVersionRequest request2 = new PatchBookChapterVersionRequest();
        request2.setContentHtml(JsonNullable.of("Patched contentHtml 2"));
        request2.setAuthorNotesStart(JsonNullable.of("Patched Start 2"));
        request2.setAuthorNotesEnd(JsonNullable.of("Patched End 2"));
        request2.setContentUpdatedAt(JsonNullable.of(ZonedDateTime.now()));

        List<BookChapterVersionDTO> updatedVersions = super.patchBulk(java.util.Map.of(version1.getId(), request1, version2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedVersions);
            assertEquals(2, updatedVersions.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        setupData();
        BookChapterVersion version = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(1).contentHtml("Test contentHtml").authorNotesStart("Start").authorNotesEnd("End").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        BookChapterVersion version2 = bookChapterVersionRepository.save(BookChapterVersion.builder().chapterVersion(2).contentHtml("Test contentHtml 2").authorNotesStart("Start").authorNotesEnd("End").chapter(defaultChapter).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(version.getId(), version2.getId())));
    }
}
