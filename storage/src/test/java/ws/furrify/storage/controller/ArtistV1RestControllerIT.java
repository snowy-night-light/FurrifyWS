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
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = StorageApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ArtistV1RestControllerIT extends BaseCrudControllerTest<Artist, ArtistDTO, CreateArtistRequest, PatchArtistRequest> {

    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    protected ArtistV1RestControllerIT(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    protected String getControllerPath() {
        return "/v1/artists";
    }

    @Override
    @Test
    protected void testCreate() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        Media avatar = mediaRepository.save(Media.builder().priority(12).fileReferenceId(UUID.randomUUID()).sources(sources).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateArtistRequest request = new CreateArtistRequest();
        request.setNicknames(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1),
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 2)
        ));
        request.setAvatar(EntityIdRequest.builder().id(avatar.getId()).build());
        request.setSources(
                sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );
        request.setLibrary(EntityIdRequest.builder().id(library.getId()).build());
        request.setFollowersCount(10);
        request.setBioHtml("Test bio");

        ArtistDTO createdArtist = super.create(request);

        assertAll(() -> {
            assertNotNull(createdArtist);
            assertEquals(2, createdArtist.getNicknames().size());
            assertEquals(avatar.getId(), createdArtist.getAvatar().getId());
            assertEquals(sources.size(), createdArtist.getSources().size());
            assertEquals(library.getId(), createdArtist.getLibrary().getId());
            assertEquals(10, createdArtist.getFollowersCount());
            assertEquals("Test bio", createdArtist.getBioHtml());
        });
    }

    @Override
    @Test
    protected void testFindById() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        ArtistDTO foundArtist = super.findById(artist.getId());

        assertAll(() -> {
            assertNotNull(foundArtist);
            assertEquals(artist.getId(), foundArtist.getId());
            assertEquals(1, foundArtist.getNicknames().size());
        });
    }

    @Override
    @Test
    protected void testFindAll() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        Page<ArtistDTO> artists = super.findAll(PageRequest.of(0, 10));

        assertAll(() -> {
            assertNotNull(artists);
            assertTrue(artists.getContent().size() >= 2);
        });
    }

    @Override
    @Test
    protected void testPatch() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        Media avatar = mediaRepository.save(Media.builder().priority(12).fileReferenceId(UUID.randomUUID()).sources(sources).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).avatar(avatar).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchArtistRequest request = new PatchArtistRequest();
        request.setNicknames(JsonNullable.of(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1),
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 2)
        )));

        Media newAvatar = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        request.setAvatar(JsonNullable.of(EntityIdRequest.builder().id(newAvatar.getId()).build()));

        List<Source> newSources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        request.setSources(
                JsonNullable.of(newSources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList())
        );
        request.setFollowersCount(JsonNullable.of(20));
        request.setBioHtml(JsonNullable.of("Patched bio"));

        ArtistDTO updatedArtist = super.patch(artist.getId(), request);

        assertAll(() -> {
            assertNotNull(updatedArtist);
            assertEquals(artist.getId(), updatedArtist.getId());
            assertEquals(2, updatedArtist.getNicknames().size());
            assertEquals(newAvatar.getId(), updatedArtist.getAvatar().getId());
            assertEquals(1, updatedArtist.getSources().size());
            assertEquals(20, updatedArtist.getFollowersCount());
            assertEquals("Patched bio", updatedArtist.getBioHtml());
        });
    }

    @Override
    @Test
    protected void testDelete() {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.delete(artist.getId()));
    }


@Override
    @Test
    protected void testCreateBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        Media avatar = mediaRepository.save(Media.builder().priority(12).fileReferenceId(UUID.randomUUID()).sources(sources).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        CreateArtistRequest request1 = new CreateArtistRequest();
        request1.setNicknames(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1),
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 2)
        ));
        request1.setAvatar(EntityIdRequest.builder().id(avatar.getId()).build());
        request1.setSources(
                sources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList()
        );
        request1.setLibrary(EntityIdRequest.builder().id(library.getId()).build());
        request1.setFollowersCount(10);
        request1.setBioHtml("Test bio");

        CreateArtistRequest request2 = new CreateArtistRequest();
        request2.setNicknames(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1)
        ));
        request2.setLibrary(EntityIdRequest.builder().id(library.getId()).build());
        request2.setFollowersCount(20);
        request2.setBioHtml("Test bio 2");

        List<ArtistDTO> createdArtists = super.createBulk(java.util.List.of(request1, request2));

        assertAll(() -> {
            assertNotNull(createdArtists);
            assertEquals(2, createdArtists.size());
        });
    }

@Override
    @Test
    protected void testPatchBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        List<Source> sources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        List<Source> sources2 = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        Media avatar = mediaRepository.save(Media.builder().priority(12).fileReferenceId(UUID.randomUUID()).sources(sources).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Media avatar2 = mediaRepository.save(Media.builder().priority(13).fileReferenceId(UUID.randomUUID()).sources(sources2).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist1 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).avatar(avatar).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).avatar(avatar2).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        PatchArtistRequest request1 = new PatchArtistRequest();
        request1.setNicknames(JsonNullable.of(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1),
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 2)
        )));

        Media newAvatar = mediaRepository.save(Media.builder().priority(1).fileReferenceId(UUID.randomUUID()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        request1.setAvatar(JsonNullable.of(EntityIdRequest.builder().id(newAvatar.getId()).build()));

        List<Source> newSources = List.of(
                sourceRepository.save(Source.builder().strategy(new MockSourceStrategyImpl()).data(new HashMap<>()).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build())
        );
        request1.setSources(
                JsonNullable.of(newSources.stream().map(source -> EntityIdRequest.builder().id(source.getId()).build()).toList())
        );
        request1.setFollowersCount(JsonNullable.of(20));
        request1.setBioHtml(JsonNullable.of("Patched bio"));

        PatchArtistRequest request2 = new PatchArtistRequest();
        request2.setNicknames(JsonNullable.of(List.of(
                ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1)
        )));
        request2.setFollowersCount(JsonNullable.of(30));

        List<ArtistDTO> updatedArtists = super.patchBulk(java.util.Map.of(artist1.getId(), request1, artist2.getId(), request2));

        assertAll(() -> {
            assertNotNull(updatedArtists);
            assertEquals(2, updatedArtists.size());
        });
    }

@Override
    @Test
    protected void testDeleteBulk() throws Exception {
        Library library = libraryRepository.save(Library.builder().title("Test library").ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());
        Artist artist2 = artistRepository.save(Artist.builder().nicknames(List.of(ArtistNickname.of(UUID.randomUUID().toString().replace("-", ""), 1))).library(library).ownerId(AuthorizationTestConfig.MOCK_SUBJECT_ID).build());

        assertDoesNotThrow(() -> super.deleteBulk(List.of(artist.getId(), artist2.getId())));
    }
}
