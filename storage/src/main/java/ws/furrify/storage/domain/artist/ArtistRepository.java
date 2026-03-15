package ws.furrify.storage.domain.artist;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface ArtistRepository extends UserScopedEntityRepository<Artist> {
}
