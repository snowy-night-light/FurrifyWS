package ws.furrify.storage.controllers.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.CreateArtistRequest;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;


@RestController
@RequestMapping("/v1/storage/artists")
class ArtistV1RestController extends BaseEntityRestController<Artist, ArtistDTO, CreateArtistRequest, PatchArtistRequest> {

    @Autowired
    public ArtistV1RestController(BaseRequestMapper<Artist, ArtistDTO, CreateArtistRequest> requestDtoMapper, BaseEntityCrudService<Artist, ArtistDTO, PatchArtistRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }

    
}
