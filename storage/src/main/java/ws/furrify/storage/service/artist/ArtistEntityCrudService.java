package ws.furrify.storage.service.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;

@Service
public class ArtistEntityCrudService extends BaseEntityCrudService<Artist, ArtistDTO> {

    @Autowired
    public ArtistEntityCrudService(BaseEntityRepository<Artist> entityRepository, BaseDTOMapper<Artist, ArtistDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
