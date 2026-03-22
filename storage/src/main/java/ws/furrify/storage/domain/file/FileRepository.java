package ws.furrify.storage.domain.file;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

@Repository
public interface FileRepository extends BaseEntityRepository<File> {
}
