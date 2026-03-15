package ws.furrify.storage.domain.media;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface MediaRepository extends UserScopedEntityRepository<Media> {
}
