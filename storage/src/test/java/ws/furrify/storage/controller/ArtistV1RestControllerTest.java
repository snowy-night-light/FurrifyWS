package ws.furrify.storage.controller;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MimeType;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.file.FileRepository;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.media.MediaRepository;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.CreateArtistRequest;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.mocks.MockSourceStrategyImpl;
import ws.furrify.testcore.config.AuthorizationTestConfig;
import ws.furrify.testcore.controller.BaseCrudControllerTest;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ArtistV1RestControllerTest extends BaseCrudControllerTest<Artist, ArtistDTO, CreateArtistRequest, PatchArtistRequest> {

    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    protected ArtistV1RestControllerTest(JsonMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/storage/artists";
    }

    @Override
    @Test
    protected void testCreate() {
        File file = fileRepository.save(File.builder().fileHash("test").fileName("test.png").fileUri(URI.create("https://example.com")).thumbnailUri(URI.create("https://example.com")).mimeType(new MimeType("image", "png")).fileSize(321L).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media avatar = mediaRepository.save(Media.builder().sources(List.of(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())).priority(0).file(file).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );

        CreateArtistRequest request = new CreateArtistRequest();
        request.setNicknames(List.of(
                ArtistNickname.of("Test_nickname", 1),
                ArtistNickname.of("Tester", 2)
        ));
        request.setAvatar(EntityIdRequest.builder().id(avatar.getId()).build());
        request.setSources(
                sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );


        ArtistDTO createdArtist = super.create(request);

        assertAll(() -> {
            assertNotNull(createdArtist);
            assertEquals(2, createdArtist.getNicknames().size());
            assertEquals(avatar.getId(), createdArtist.getAvatar().getId());
            assertEquals(sources.size(), createdArtist.getSources().size());
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
        Artist artist = artistRepository.save(Artist.builder().ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<ArtistDTO> artists = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(artists);
            assertEquals(2, artists.getContent().size());
        });
    }

    @Override
    @Test
    protected void testPatch() {
        File file = fileRepository.save(File.builder().fileHash("test").fileName("test.png").fileUri(URI.create("https://example.com")).thumbnailUri(URI.create("https://example.com")).mimeType(new MimeType("image", "png")).fileSize(321L).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media avatar = mediaRepository.save(Media.builder().sources(List.of(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())).priority(0).file(file).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().avatar(avatar).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchArtistRequest request = new PatchArtistRequest();
        request.setNicknames(JsonNullable.of(List.of(
                ArtistNickname.of("Test_nickname", 1),
                ArtistNickname.of("Tester", 2)
        )));

        File newFile = fileRepository.save(File.builder().fileHash("test").fileName("test.png").fileUri(URI.create("https://example.com")).thumbnailUri(URI.create("https://example.com")).mimeType(new MimeType("image", "png")).fileSize(321L).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media newAvatar = mediaRepository.save(Media.builder().priority(1).file(newFile).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        request.setAvatar(JsonNullable.of(EntityIdRequest.builder().id(newAvatar.getId()).build()));

        List<Source> newSources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        request.setSources(
                JsonNullable.of(newSources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList())
        );

        ArtistDTO updatedArtist = super.patch(artist.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedArtist);
            assertEquals(artist.getId(), updatedArtist.getId());
            assertEquals(newAvatar.getId(), updatedArtist.getAvatar().getId());
            assertEquals(2, updatedArtist.getNicknames().size());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Artist artist = artistRepository.save(Artist.builder().ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(artist.getId()));
    }
}
