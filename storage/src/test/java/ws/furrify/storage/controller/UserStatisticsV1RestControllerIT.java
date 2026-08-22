package ws.furrify.storage.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.model.PagedModelAttachmentFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
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
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseControllerTest;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class UserStatisticsV1RestControllerIT extends BaseControllerTest {

    @MockitoBean
    private AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Autowired
    protected UserStatisticsV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/user/{userId}/statistics";
    }

    private String randomName() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    @BeforeEach
    public void setupData() {
        artistRepository.deleteAll();
        collectionRepository.deleteAll();
        postRepository.deleteAll();
        tagRepository.deleteAll();
        tagCategoryRepository.deleteAll();
        libraryRepository.deleteAll();

        Library library = libraryRepository.save(Library.builder().title("Test library " + randomName()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory category = tagCategoryRepository.save(TagCategory.builder().name(randomName()).hexColor("#FFFFFF").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(randomName()).category(category).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Post post = postRepository.save(Post.builder().title("Test title").tags(List.of(tag)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        collectionRepository.save(Collection.builder().title("Test collection").posts(List.of(post)).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of("Test", 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
    }

    @Test
    public void testGetUserStatistics() {
        long postsInDb = postRepository.count();
        long collectionsInDb = collectionRepository.count();
        long librariesInDb = libraryRepository.count();
        long tagsInDb = tagRepository.count();
        long artistsInDb = artistRepository.count();

        PagedModelAttachmentFileDTO pagedModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 0}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), any())).thenReturn(org.springframework.http.ResponseEntity.ok(pagedModel));

        RestAssured.given()
                .when()
                .get(basePath.replace("{userId}", AuthorizationTestConfig.MOCK_SUBJECT_ID.toString()))
                .then()
                .statusCode(200)
                .body("ownerId", Matchers.equalTo(AuthorizationTestConfig.MOCK_SUBJECT_ID.toString()))
                .body("postsCount", Matchers.equalTo((int)postsInDb))
                .body("collectionsCount", Matchers.equalTo((int) collectionsInDb))
                .body("librariesCount", Matchers.equalTo((int)librariesInDb))
                .body("tagsCount", Matchers.equalTo((int)tagsInDb))
                .body("artistsCount", Matchers.equalTo((int) artistsInDb))
                .body("last7DaysChart", Matchers.notNullValue());
    }

    @Test
    public void testGetUserStatisticsUnauthorized() {
        RestAssured.given()
                .when()
                .get(basePath.replace("{userId}", UUID.randomUUID().toString()))
                .then()
                .statusCode(403);
    }

    @Test
    public void testGetUserStatisticsChartData() {
        PagedModelAttachmentFileDTO pagedModel= jsonMapper.readValue("{\"page\": {\"totalElements\": 0}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), any())).thenReturn(org.springframework.http.ResponseEntity.ok(pagedModel));

        RestAssured.given()
                .when()
                .get(basePath.replace("{userId}", AuthorizationTestConfig.MOCK_SUBJECT_ID.toString()))
                .then()
                .statusCode(200)
                .body("last7DaysChart", Matchers.hasSize(7))
                .body("last7DaysChart[0].newPostsCount", Matchers.equalTo(0))
                .body("last7DaysChart[6].newPostsCount", Matchers.equalTo(1))
                .body("last7DaysChart[6].newCollectionsCount", Matchers.equalTo(1))
                .body("last7DaysChart[6].newTagsCount", Matchers.equalTo(1))
                .body("last7DaysChart[6].newArtistsCount", Matchers.equalTo(1));
    }

    @Test
    public void testGetUserStatisticsAttachmentCounts() throws Exception {
        PagedModelAttachmentFileDTO defaultModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 0}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), any())).thenReturn(org.springframework.http.ResponseEntity.ok(defaultModel));

        PagedModelAttachmentFileDTO gifModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 5}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), contains("image/gif"))).thenReturn(org.springframework.http.ResponseEntity.ok(gifModel));

        PagedModelAttachmentFileDTO flashModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 3}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), contains("application/x-shockwave-flash"))).thenReturn(org.springframework.http.ResponseEntity.ok(flashModel));

        PagedModelAttachmentFileDTO imagesModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 15}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), contains("image/%"))).thenReturn(org.springframework.http.ResponseEntity.ok(imagesModel));

        PagedModelAttachmentFileDTO videoModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 4}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), contains("video/%"))).thenReturn(org.springframework.http.ResponseEntity.ok(videoModel));

        PagedModelAttachmentFileDTO musicModel = jsonMapper.readValue("{\"page\": {\"totalElements\": 7}}", PagedModelAttachmentFileDTO.class);
        Mockito.when(attachmentFileV1RestControllerApiClient.getAllPaged(any(), contains("audio/%"))).thenReturn(org.springframework.http.ResponseEntity.ok(musicModel));

        RestAssured.given()
                .when()
                .get(basePath.replace("{userId}", AuthorizationTestConfig.MOCK_SUBJECT_ID.toString()))
                .then()
                .statusCode(200)
                .body("animationCount", Matchers.equalTo(8))
                .body("imagesCount", Matchers.equalTo(10))
                .body("videoCount", Matchers.equalTo(4))
                .body("musicCount", Matchers.equalTo(7));
    }
}
