package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.domain.tag.alias.TagAliasRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.CreateTagAliasRequest;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TagAliasV1RestControllerIT extends BaseCrudControllerTest<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> {

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagAliasRepository tagAliasRepository;

    @Autowired
    protected TagAliasV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/tags/aliases";
    }

    @Override
    @Test
    protected void testCreate() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test543").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().category(tagCategory).name("test123").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagAliasRequest request = new CreateTagAliasRequest();
        request.setAlias("test591");
        request.setTargetTag(EntityIdRequest.builder().id(tag.getId()).build());

        TagAliasDTO createdTagAlias = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTagAlias);
            assertEquals(tag.getId(), createdTagAlias.getTargetTag().getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test295").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test000").category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias("test295alias").targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        TagAliasDTO foundTagAlias = super.findById(tagAlias.getId());

        assertAll(() -> {
            assertNotNull(foundTagAlias);
            assertEquals(tagAlias.getId(), foundTagAlias.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test111").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test999").category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagAliasRepository.save(TagAlias.builder().alias("test111alias1").targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagAliasRepository.save(TagAlias.builder().alias("test111alias2").targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<TagAliasDTO> aliases = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(aliases);
            assertEquals(2, aliases.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test222").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test888").category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias("test222alias").targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagAliasRequest request = new PatchTagAliasRequest();
        request.setAlias(JsonNullable.of("newaliasname"));

        TagAliasDTO updatedTagAlias = super.patch(tagAlias.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedTagAlias);
            assertEquals(tagAlias.getId(), updatedTagAlias.getId());
            assertEquals("newaliasname", updatedTagAlias.getAlias());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name("test444").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name("test555").category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias("test444alias").targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tagAlias.getId()));
    }
}
