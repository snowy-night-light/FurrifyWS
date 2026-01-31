package ws.furrify.storage.domain.file;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseRepository;
import ws.furrify.storage.domain.media.Media;

@Repository
public interface FileRepository extends BaseRepository<File> {
}
