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
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.request.CreateSourceRequest;
import ws.furrify.storage.dto.source.request.PatchSourceRequest;
import ws.furrify.storage.mocks.MockSourceStrategyImpl;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class SourceV1RestControllerIT extends BaseCrudControllerTest<Source, SourceDTO, CreateSourceRequest, PatchSourceRequest> {

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    protected SourceV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/sources";
    }

    @Override
    @Test
    protected void testCreate() {
        CreateSourceRequest request = new CreateSourceRequest();
        request.setStrategy(new MockSourceStrategyImpl());
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        request.setData(data);

        SourceDTO createdSource = super.create(request);

        assertAll(() -> {
            assertNotNull(createdSource);
            assertNotNull(createdSource.getId());
            assertEquals("test", createdSource.getData().get("test"));
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        Source source = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        SourceDTO foundSource = super.findById(source.getId());

        assertAll(() -> {
            assertNotNull(foundSource);
            assertEquals(source.getId(), foundSource.getId());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<SourceDTO> sources = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(sources);
            assertEquals(2, sources.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        Source source = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchSourceRequest request = new PatchSourceRequest();
        Map<String, Object> newData = new HashMap<>();
        newData.put("patched", "value");
        request.setData(JsonNullable.of(newData));

        SourceDTO updatedSource = super.patch(source.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedSource);
            assertEquals(source.getId(), updatedSource.getId());
            assertEquals("value", updatedSource.getData().get("patched"));
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        Source source = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(source.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        CreateSourceRequest request1 = new CreateSourceRequest();
        request1.setStrategy(new MockSourceStrategyImpl());
        Map<String, Object> data1 = new HashMap<>();
        data1.put("test", "test1");
        request1.setData(data1);

        CreateSourceRequest request2 = new CreateSourceRequest();
        request2.setStrategy(new MockSourceStrategyImpl());
        Map<String, Object> data2 = new HashMap<>();
        data2.put("test", "test2");
        request2.setData(data2);

        List<SourceDTO> createdSources = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdSources);
            assertEquals(2, createdSources.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("test", "test1");
        Source source1 = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data1).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Map<String, Object> data2 = new HashMap<>();
        data2.put("test", "test2");
        Source source2 = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data2).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchSourceRequest request1 = new PatchSourceRequest();
        Map<String, Object> newData1 = new HashMap<>();
        newData1.put("patched", "value1");
        request1.setData(JsonNullable.of(newData1));

        PatchSourceRequest request2 = new PatchSourceRequest();
        Map<String, Object> newData2 = new HashMap<>();
        newData2.put("patched", "value2");
        request2.setData(JsonNullable.of(newData2));

        List<SourceDTO> updatedSources = super.patchBulk(java.util.Map.of(source1.getId(), request1, source2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedSources);
            assertEquals(2, updatedSources.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        Source source = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Source source2 = sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(data).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(java.util.List.of(source.getId(), source2.getId())));
    }
}
