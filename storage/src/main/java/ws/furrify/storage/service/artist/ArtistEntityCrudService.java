package ws.furrify.storage.service.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.UserScopedEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;

@Service
public class ArtistEntityCrudService extends UserScopedEntityCrudService<Artist, ArtistDTO> {

    @Autowired
    public ArtistEntityCrudService(UserScopedEntityRepository<Artist> entityRepository, BaseDTOMapper<Artist, ArtistDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
