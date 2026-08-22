package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.domain.tag.alias.TagAliasRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.CreateTagRequest;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TagV1RestControllerIT extends BaseCrudControllerTest<Tag, TagDTO, CreateTagRequest, PatchTagRequest> {

    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagAliasRepository tagAliasRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    protected TagV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/storage/tags";
    }

    @Override
    @Test
    protected void testCreate() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<TagAlias> aliases = List.of(
                tagAliasRepository.save(TagAlias.builder().alias("test123").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test543").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagRequest request = new CreateTagRequest();
        request.setName("test591");
        request.setCategory(EntityIdRequest.builder().id(tagCategory.getId()).build());
        request.setAliases(
                aliases.stream().map(alias -> EntityIdRequest.builder().id(alias.getId()).build()).toList()
        );
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        TagDTO createdTag = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTag);
            assertEquals(request.getAliases().size(), createdTag.getAliases().size());
            assertEquals(tagCategory.getId(), createdTag.getCategory().getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test295").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test000").category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        TagDTO foundTag = super.findById(tag.getId());

        assertAll(() -> {
            assertNotNull(foundTag);
            assertEquals(tag.getId(), foundTag.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test111").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test999").category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name("test777").category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<TagDTO> tags = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(tags);
            assertEquals(2, tags.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<TagAlias> aliases = List.of(
                tagAliasRepository.save(TagAlias.builder().alias("test779").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        List<TagAlias> aliases2 = List.of(
                tagAliasRepository.save(TagAlias.builder().alias("test664").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test222").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory2 = tagCategoryRepository.save(TagCategory.builder().hexColor("#c3c").name("test333").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test888").category(tagCategory).aliases(aliases).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagRequest request = new PatchTagRequest();
        request.setCategory(JsonNullable.of(EntityIdRequest.builder().id(tagCategory2.getId()).build()));
        request.setAliases(
                JsonNullable.of(aliases2.stream().map(alias -> EntityIdRequest.builder().id(alias.getId()).build()).toList())
        );

        TagDTO updatedTag = super.patch(tag.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedTag);
            assertEquals(tag.getId(), updatedTag.getId());
            assertEquals(tagCategory2.getId(), updatedTag.getCategory().getId());
            assertEquals(aliases2.stream().map(BaseEntity::getId).toList(), updatedTag.getAliases().stream().map(BaseEntityDTO::getId).toList());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test444").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test555").category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tag.getId()));
    }
}
