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
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.alias.TagAliasRepository;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.CreateTagRequest;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

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
        return "/v1/tags";
    }

    @Override
    @Test
    protected void testCreate() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagRequest request = new CreateTagRequest();
        request.setName(UUID.randomUUID().toString().replace("-", ""));
        request.setCategory(EntityIdRequest.builder().id(tagCategory.getId()).build());
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        TagDTO createdTag = super.create(request);

        assertAll(() -> {
            assertNotNull(createdTag);
            assertEquals(tagCategory.getId(), createdTag.getCategory().getId());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

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
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory2 = tagCategoryRepository.save(TagCategory.builder().hexColor("#c3c").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagRequest request = new PatchTagRequest();
        request.setCategory(JsonNullable.of(EntityIdRequest.builder().id(tagCategory2.getId()).build()));

        TagDTO updatedTag = super.patch(tag.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedTag);
            assertEquals(tag.getId(), updatedTag.getId());
            assertEquals(tagCategory2.getId(), updatedTag.getCategory().getId());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(tag.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateTagRequest request1 = new CreateTagRequest();
        request1.setName(UUID.randomUUID().toString().replace("-", ""));
        request1.setCategory(EntityIdRequest.builder().id(tagCategory.getId()).build());
        request1.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        CreateTagRequest request2 = new CreateTagRequest();
        request2.setName(UUID.randomUUID().toString().replace("-", ""));
        request2.setCategory(EntityIdRequest.builder().id(tagCategory.getId()).build());
        request2.setLibrary(EntityIdRequest.builder().id(library.getId()).build());

        List<TagDTO> createdTags = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdTags);
            assertEquals(2, createdTags.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory1 = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory2 = tagCategoryRepository.save(TagCategory.builder().hexColor("#c3c").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag1 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory1).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory1).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchTagRequest request1 = new PatchTagRequest();
        request1.setCategory(JsonNullable.of(EntityIdRequest.builder().id(tagCategory2.getId()).build()));

        PatchTagRequest request2 = new PatchTagRequest();
        request2.setCategory(JsonNullable.of(EntityIdRequest.builder().id(tagCategory2.getId()).build()));

        List<TagDTO> updatedTags = super.patchBulk(java.util.Map.of(tag1.getId(), request1, tag2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedTags);
            assertEquals(2, updatedTags.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        TagCategory tagCategory = tagCategoryRepository.save(TagCategory.builder().hexColor("#3c3").name(UUID.randomUUID().toString().replace("-", "")).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Tag tag2 = tagRepository.save(Tag.builder().name(UUID.randomUUID().toString().replace("-", "")).category(tagCategory).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(tag.getId(), tag2.getId())));
    }
}
