package ws.furrify.storage.domain.artist;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

@Repository
public interface ArtistRepository extends BaseEntityRepository<Artist> {
}
