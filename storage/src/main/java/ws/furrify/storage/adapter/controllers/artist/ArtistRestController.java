package ws.furrify.storage.adapter.controllers.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.ArtistDTOMapper;
import ws.furrify.storage.dto.artist.PatchArtistRequestDTO;


@RestController
@RequestMapping("/storage/artists")
class ArtistRestController extends BaseEntityRestController<Artist, ArtistDTO, PatchArtistRequestDTO> {

    @Autowired
    public ArtistRestController(final ArtistDTOMapper mapper, final ArtistRepository repository) {
        super(mapper, repository);
    }
}
