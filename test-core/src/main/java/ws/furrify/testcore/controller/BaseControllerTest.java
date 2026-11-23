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

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.config.PostgresTestConfig;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = {PostgresTestConfig.class, AuthorizationTestConfig.class})
public abstract class BaseControllerTest {

    protected final JsonMapper jsonMapper;

    protected final String basePath;

    @LocalServerPort
    protected Integer port;

    @SuppressWarnings("unchecked")
    protected BaseControllerTest(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.basePath = getControllerPath();
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
}