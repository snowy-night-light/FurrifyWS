package ws.furrify.storage.domain.tag;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface TagRepository extends UserScopedEntityRepository<Tag> {
}
