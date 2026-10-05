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
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.collection.CollectionRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.post.PostRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.collection.request.CreateCollectionRequest;
import ws.furrify.storage.dto.collection.request.PatchCollectionRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CollectionV1RestControllerIT extends BaseCrudControllerTest<Collection, CollectionDTO, CreateCollectionRequest, PatchCollectionRequest> {

    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    protected CollectionV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/collections";
    }

    private String randomName() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    @Override
    @Test
    protected void testCreate() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateCollectionRequest request = new CreateCollectionRequest();
        request.setTitle("Test collection");
        request.setPosts(List.of(EntityIdRequest.builder().id(post.getId()).build()));
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        CollectionDTO createdCollection = super.create(request);

        assertAll(() -> {
            assertNotNull(createdCollection);
            assertEquals("Test collection", createdCollection.getTitle());
            assertEquals(1, createdCollection.getPosts().size());
            assertEquals(post.getId(), createdCollection.getPosts().get(0).getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection = collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CollectionDTO foundCollection = super.findById(collection.getId());

        assertAll(() -> {
            assertNotNull(foundCollection);
            assertEquals(collection.getId(), foundCollection.getId());
            assertEquals("Test collection", foundCollection.getTitle());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post2 = postRepository.save(Post.builder().title("Test title 2").tags(List.of(tag2)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        collectionRepository.save(Collection.builder().title("Test collection 2").posts(List.of(post2)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<CollectionDTO> collections = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(collections);
            assertTrue(collections.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection = collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchCollectionRequest request = new PatchCollectionRequest();
        request.setTitle(JsonNullable.of("Patched title"));

        CollectionDTO updatedCollection = super.patch(collection.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedCollection);
            assertEquals(collection.getId(), updatedCollection.getId());
            assertEquals("Patched title", updatedCollection.getTitle());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection = collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(collection.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post1 = postRepository.save(Post.builder().title("Test title 1").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post2 = postRepository.save(Post.builder().title("Test title 2").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateCollectionRequest request1 = new CreateCollectionRequest();
        request1.setTitle("Test collection 1");
        request1.setPosts(List.of(EntityIdRequest.builder().id(post1.getId()).build()));
        request1.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        CreateCollectionRequest request2 = new CreateCollectionRequest();
        request2.setTitle("Test collection 2");
        request2.setPosts(List.of(EntityIdRequest.builder().id(post2.getId()).build()));
        request2.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        List<CollectionDTO> createdCollections = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdCollections);
            assertEquals(2, createdCollections.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection1 = collectionRepository.save(Collection.builder().title("Test collection 1").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection2 = collectionRepository.save(Collection.builder().title("Test collection 2").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchCollectionRequest request1 = new PatchCollectionRequest();
        request1.setTitle(JsonNullable.of("Patched title 1"));

        PatchCollectionRequest request2 = new PatchCollectionRequest();
        request2.setTitle(JsonNullable.of("Patched title 2"));

        List<CollectionDTO> updatedCollections = super.patchBulk(java.util.Map.of(collection1.getId(), request1, collection2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedCollections);
            assertEquals(2, updatedCollections.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection = collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Collection collection2 = collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(collection.getId(), collection2.getId())));
    }
}
