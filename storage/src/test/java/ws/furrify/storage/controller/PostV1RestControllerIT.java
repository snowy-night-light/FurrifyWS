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
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.post.PostRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.request.CreatePostRequest;
import ws.furrify.storage.dto.post.request.PatchPostRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PostV1RestControllerIT extends BaseCrudControllerTest<Post, PostDTO, CreatePostRequest, PatchPostRequest> {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    protected PostV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/posts";
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

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test title");
        request.setDescription("Test description");
        request.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        PostDTO createdPost = super.create(request);

        assertAll(() -> {
            assertNotNull(createdPost);
            assertEquals("Test title", createdPost.getTitle());
            assertEquals("Test description", createdPost.getDescription());
            assertEquals(1, createdPost.getTags().size());
            assertEquals(tag.getId(), createdPost.getTags().get(0).getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PostDTO foundPost = super.findById(post.getId());

        assertAll(() -> {
            assertNotNull(foundPost);
            assertEquals(post.getId(), foundPost.getId());
            assertEquals("Test title", foundPost.getTitle());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        postRepository.save(Post.builder().title("Test title 2").tags(List.of(tag2)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<PostDTO> posts = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(posts);
            assertTrue(posts.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchPostRequest request = new PatchPostRequest();
        request.setTitle(JsonNullable.of("Patched title"));

        PostDTO updatedPost = super.patch(post.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedPost);
            assertEquals(post.getId(), updatedPost.getId());
            assertEquals("Patched title", updatedPost.getTitle());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(post.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("Test title 1");
        request1.setDescription("Test description 1");
        request1.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request1.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        CreatePostRequest request2 = new CreatePostRequest();
        request2.setTitle("Test title 2");
        request2.setDescription("Test description 2");
        request2.setTags(List.of(EntityIdRequest.builder().id(tag.getId()).build()));
        request2.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        List<PostDTO> createdPosts = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdPosts);
            assertEquals(2, createdPosts.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post1 = postRepository.save(Post.builder().title("Test title 1").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post2 = postRepository.save(Post.builder().title("Test title 2").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchPostRequest request1 = new PatchPostRequest();
        request1.setTitle(JsonNullable.of("Patched title 1"));

        PatchPostRequest request2 = new PatchPostRequest();
        request2.setTitle(JsonNullable.of("Patched title 2"));

        List<PostDTO> updatedPosts = super.patchBulk(java.util.Map.of(post1.getId(), request1, post2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedPosts);
            assertEquals(2, updatedPosts.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post2 = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(post.getId(), post2.getId())));
    }
}
