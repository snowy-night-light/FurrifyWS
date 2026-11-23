/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.furrify.testcore.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.model.RestPageImpl;
import ws.furrify.core.utils.EntitySpecUtils;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.config.PostgresTestConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = {PostgresTestConfig.class, AuthorizationTestConfig.class})
public abstract class BaseCrudControllerTest<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, CREATE_REQ extends BaseCreateEntityRequest<ENTITY, DTO>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> extends BaseControllerTest {

    protected final Class<DTO> dtoClass;
    private final Type pageType;

    @SuppressWarnings("unchecked")
    protected BaseCrudControllerTest(JsonMapper jsonMapper) {
        super(jsonMapper);
        ResolvableType type = ResolvableType.forClass(getClass()).as(BaseCrudControllerTest.class);

        this.dtoClass = (Class<DTO>) type.getGeneric(1).resolve();
        this.pageType = jsonMapper.getTypeFactory().constructParametricType(RestPageImpl.class, this.dtoClass);
    }

    protected abstract void testCreate() throws IOException, URISyntaxException;

    protected abstract void testFindById();

    protected abstract void testFindAll();

    protected abstract void testPatch();

    protected abstract void testDelete();

    protected abstract void testCreateBulk() throws Exception;

    protected abstract void testPatchBulk() throws Exception;

    protected abstract void testDeleteBulk() throws Exception;


    protected DTO findById(UUID id) {
        return given()
                .header("Content-Type", "application/json")
                .pathParam("id", id)
                .when()
                .get(this.basePath + "/{id}")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(this.dtoClass);
    }

    protected Page<DTO> findAll(Pageable pageable) {
        return findAll(null, pageable);
    }

    protected Page<DTO> findAll(String spec) {
        return findAll(spec, Pageable.unpaged());
    }

    protected Page<DTO> findAll(String spec, Pageable pageable) {
        var request = given()
                .header("Content-Type", "application/json");
        if (spec != null) {
            request.header(EntitySpecUtils.SPEC_HEADER, EntitySpecUtils.encodeSpecToBase64(spec));
        }
        if (pageable.isPaged()) {
            request.queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());
        }
        pageable.getSort().forEach(order -> request.queryParam("sort", order.getProperty() + "," + order.getDirection().name().toLowerCase()));
        return request
                .when()
                .get(this.basePath)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(pageType);
    }

    protected DTO create(CREATE_REQ createReq) {
        return given()
                .header("Content-Type", "application/json")
                .body(createReq)
                .when()
                .post(this.basePath)
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(this.dtoClass);
    }

    protected DTO patch(UUID id, PATCH_REQ patchReq) {
        return given()
                .header("Content-Type", "application/json")
                .pathParam("id", id)
                .body(patchReq)
                .when()
                .patch(this.basePath + "/{id}")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(this.dtoClass);
    }

    protected void delete(UUID id) {
        given()
                .header("Content-Type", "application/json")
                .pathParam("id", id)
                .when()
                .delete(this.basePath + "/{id}")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

    protected java.util.List<DTO> createBulk(java.util.List<CREATE_REQ> createReqs) {
        return java.util.Arrays.asList(given()
                .header("Content-Type", "application/json")
                .body(createReqs)
                .when()
                .post(this.basePath + "/bulk")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(jsonMapper.getTypeFactory().constructArrayType(this.dtoClass)));
    }

    protected java.util.List<DTO> patchBulk(java.util.Map<UUID, PATCH_REQ> patchReqs) {
        return java.util.Arrays.asList(given()
                .header("Content-Type", "application/json")
                .body(patchReqs)
                .when()
                .patch(this.basePath + "/bulk")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(jsonMapper.getTypeFactory().constructArrayType(this.dtoClass)));
    }

    protected void deleteBulk(java.util.List<UUID> ids) {
        given()
                .header("Content-Type", "application/json")
                .body(ids)
                .when()
                .delete(this.basePath + "/bulk")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

}
