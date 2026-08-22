package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.request.CreateTagCategoryRequest;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TagCategoryV1RestControllerIT extends BaseCrudControllerTest<TagCategory, TagCategoryDTO, CreateTagCategoryRequest, PatchTagCategoryRequest> {

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Autowired
    protected TagCategoryV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/tags/categories";
    }

    @Override
    @Test
    protected void testCreate() {
        CreateTagCategoryRequest request = new CreateTagCategoryRequest();
        request.setName("test591_create");
        request.setHexColor("#123456");

        TagCategoryDTO createdTagCategory = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTagCategory);
            assertEquals("test591_create", createdTagCategory.getName());
            assertEquals("#123456", createdTagCategory.getHexColor());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test295_find").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        TagCategoryDTO foundTagCategory = super.findById(tagCategory.getId());

        assertAll(() -> {
            assertNotNull(foundTagCategory);
            assertEquals(tagCategory.getId(), foundTagCategory.getId());
            assertEquals(tagCategory.getName(), foundTagCategory.getName());
            assertEquals(tagCategory.getHexColor(), foundTagCategory.getHexColor());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test111_findAll").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test222_findAll").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<TagCategoryDTO> categories = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(categories);
            assertTrue(categories.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test222_patch").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagCategoryRequest request = new PatchTagCategoryRequest();
        request.setName(JsonNullable.of("newcategoryname"));
        request.setHexColor(JsonNullable.of("#000000"));

        TagCategoryDTO updatedTagCategory = super.patch(tagCategory.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedTagCategory);
            assertEquals(tagCategory.getId(), updatedTagCategory.getId());
            assertEquals("newcategoryname", updatedTagCategory.getName());
            assertEquals("#000000", updatedTagCategory.getHexColor());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test444_delete").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tagCategory.getId()));
    }
}
