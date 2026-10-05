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

import java.util.List;
import java.util.UUID;

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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().category(tagCategory).name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagAliasRequest request = new CreateTagAliasRequest();
        request.setAlias(UUID.randomUUID().toString().replace("-", ""));
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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        TagAliasDTO foundTagAlias = super.findById(tagAlias.getId());

        assertAll(() -> {
            assertNotNull(foundTagAlias);
            assertEquals(tagAlias.getId(), foundTagAlias.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<TagAliasDTO> aliases = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(aliases);
            assertEquals(2, aliases.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tagAlias.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag1 = tagRepository.save(Tag.builder().category(tagCategory).name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().category(tagCategory).name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagAliasRequest request1 = new CreateTagAliasRequest();
        request1.setAlias(UUID.randomUUID().toString().replace("-", ""));
        request1.setTargetTag(EntityIdRequest.builder().id(tag1.getId()).build());

        CreateTagAliasRequest request2 = new CreateTagAliasRequest();
        request2.setAlias(UUID.randomUUID().toString().replace("-", ""));
        request2.setTargetTag(EntityIdRequest.builder().id(tag2.getId()).build());

        List<TagAliasDTO> createdTagAliases = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdTagAliases);
            assertEquals(2, createdTagAliases.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag1 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias1 = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag1).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias2 = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag2).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagAliasRequest request1 = new PatchTagAliasRequest();
        request1.setAlias(JsonNullable.of("newaliasnamebulk1"));

        PatchTagAliasRequest request2 = new PatchTagAliasRequest();
        request2.setAlias(JsonNullable.of("newaliasnamebulk2"));

        List<TagAliasDTO> updatedTagAliases = super.patchBulk(java.util.Map.of(tagAlias1.getId(), request1, tagAlias2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedTagAliases);
            assertEquals(2, updatedTagAliases.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagAlias tagAlias2 = tagAliasRepository.save(TagAlias.builder().alias(UUID.randomUUID().toString().replace("-", "")).targetTag(tag).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(tagAlias.getId(), tagAlias2.getId())));
    }
}
