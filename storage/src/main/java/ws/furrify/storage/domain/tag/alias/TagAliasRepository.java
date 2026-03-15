package ws.furrify.storage.domain.tag.alias;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface TagAliasRepository extends UserScopedEntityRepository<TagAlias> {
}
