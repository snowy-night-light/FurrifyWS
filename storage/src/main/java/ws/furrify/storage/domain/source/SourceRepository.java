package ws.furrify.storage.domain.source;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface SourceRepository extends UserScopedEntityRepository<Source> {
}
