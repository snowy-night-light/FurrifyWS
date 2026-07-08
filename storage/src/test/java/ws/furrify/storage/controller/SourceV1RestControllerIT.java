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
        return "/v1/storage/sources";
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
}
