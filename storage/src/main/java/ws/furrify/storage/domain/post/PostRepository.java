package ws.furrify.storage.domain.post;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface PostRepository extends UserScopedEntityRepository<Post> {

}
