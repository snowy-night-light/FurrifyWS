/*
 * furrify-test-core - Furrify Workspace Project
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
        return given()
                .header("Content-Type", "application/json")
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
