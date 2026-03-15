package ws.furrify.storage.domain.tag.category;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface TagCategoryRepository extends UserScopedEntityRepository<TagCategory> {
}
