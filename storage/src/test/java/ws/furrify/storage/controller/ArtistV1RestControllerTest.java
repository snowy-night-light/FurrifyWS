package ws.furrify.storage.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

public class ArtistV1RestControllerTest extends BaseCrudControllerTest {

    @Override
    protected void setControllerPath() {
        RestAssured.basePath = "/v1/storage/artists";
    }

    @Override
    @Test
    protected void testCreate() {

    }

    @Override
    @Test
    protected void testFindById() {

    }

    @Override
    @Test
    protected void testFindAll() {

    }

    @Override
    @Test
    protected void testPatch() {

    }

    @Override
    @Test
    protected void testDelete() {

    }
}
