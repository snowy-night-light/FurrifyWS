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
        return "/v1/storage/posts";
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
}
