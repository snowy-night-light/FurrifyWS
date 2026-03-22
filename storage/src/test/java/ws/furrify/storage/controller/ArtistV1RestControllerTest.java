package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.CreateArtistRequest;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistV1RestControllerTest extends BaseCrudControllerTest<Artist, ArtistDTO, CreateArtistRequest, PatchArtistRequest> {

    @Autowired
    private ArtistRepository artistRepository;

    @Override
    protected String getControllerPath() {
        return "/v1/storage/artists";
    }

    @Override
    @Test
    protected void testCreate() {
        CreateArtistRequest request = new CreateArtistRequest();

        ArtistDTO createdArtist = super.create(request);

        assertAll(() -> {
            assertNotNull(createdArtist);
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Artist artist = artistRepository.save(Artist.builder().ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        ArtistDTO foundArtist = super.findById(artist.getId());

        assertAll(() -> {
            assertNotNull(foundArtist);
            assertEquals(artist.getId(), foundArtist.getId());
        });
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
