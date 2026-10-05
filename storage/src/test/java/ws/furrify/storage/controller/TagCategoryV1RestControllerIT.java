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
    private LibraryRepository libraryRepository;

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
        Library library = libraryRepository.save(Library.builder().title("Test lib22rary").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagCategoryRequest request = new CreateTagCategoryRequest();
        String randomName = UUID.randomUUID().toString().replace("-", "");
        request.setName(randomName);
        request.setHexColor("#123456");
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        TagCategoryDTO createdTagCategory = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTagCategory);
            assertEquals(randomName, createdTagCategory.getName());
            assertEquals("#123456", createdTagCategory.getHexColor());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<TagCategoryDTO> categories = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(categories);
            assertTrue(categories.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagCategoryRequest request = new PatchTagCategoryRequest();
        request.setName(JsonNullable.of("newcategoryname"));
        request.setHexColor(JsonNullable.of("#000000"));
        request.setLibrary(JsonNullable.of(EntityIdRequest.builder().id(library.getId()).build()));

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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tagCategory.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test lib22rary").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagCategoryRequest request1 = new CreateTagCategoryRequest();
        String randomName1 = UUID.randomUUID().toString().replace("-", "");
        request1.setName(randomName1);
        request1.setHexColor("#123456");
        request1.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        CreateTagCategoryRequest request2 = new CreateTagCategoryRequest();
        String randomName2 = UUID.randomUUID().toString().replace("-", "");
        request2.setName(randomName2);
        request2.setHexColor("#654321");
        request2.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        List<TagCategoryDTO> createdTagCategories = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdTagCategories);
            assertEquals(2, createdTagCategories.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory1 = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory2 = tagCategoryRepository.save(TagCategory.builder().hexColor("#4c4").name(UUID.randomUUID().toString().replace("-", "")).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagCategoryRequest request1 = new PatchTagCategoryRequest();
        request1.setHexColor(JsonNullable.of("#000000"));
        request1.setLibrary(JsonNullable.of(EntityIdRequest.builder().id(library.getId()).build()));

        PatchTagCategoryRequest request2 = new PatchTagCategoryRequest();
        request2.setHexColor(JsonNullable.of("#111111"));
        request2.setLibrary(JsonNullable.of(EntityIdRequest.builder().id(library.getId()).build()));

        List<TagCategoryDTO> updatedTagCategories = super.patchBulk(java.util.Map.of(tagCategory1.getId(), request1, tagCategory2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedTagCategories);
            assertEquals(2, updatedTagCategories.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory2 = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(tagCategory.getId(), tagCategory2.getId())));
    }
}
