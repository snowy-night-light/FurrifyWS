package ws.furrify.testcore.controller;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

import java.lang.reflect.Type;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = {PostgresTestConfig.class, AuthorizationTestConfig.class})
public abstract class BaseCrudControllerTest<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, CREATE_REQ extends BaseCreateEntityRequest<ENTITY, DTO>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> {

    protected final JsonMapper jsonMapper;

    private final String basePath;
    private final Class<DTO> dtoClass;
    private final Type pageType;

    @LocalServerPort
    protected Integer port;

    @SuppressWarnings("unchecked")
    protected BaseCrudControllerTest(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        ResolvableType type = ResolvableType.forClass(getClass()).as(BaseCrudControllerTest.class);

        this.dtoClass = (Class<DTO>) type.getGeneric(1).resolve();
        this.basePath = getControllerPath();
        this.pageType = jsonMapper.getTypeFactory().constructParametricType(RestPageImpl.class, this.dtoClass);
    }

    @BeforeEach
    protected void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson3ObjectMapperFactory((type, charset) -> jsonMapper)
        );
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer mock-token")
                .build();
    }

    protected abstract String getControllerPath();

    protected abstract void testCreate();

    protected abstract void testFindById();

    protected abstract void testFindAll();

    protected abstract void testPatch();

    protected abstract void testDelete();

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
}