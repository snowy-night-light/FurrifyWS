package ws.furrify.storage.domain.file;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.UserScopedEntityRepository;

@Repository
public interface FileRepository extends UserScopedEntityRepository<File> {
}
