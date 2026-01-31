package ws.furrify.storage.domain.source;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseRepository;
import ws.furrify.storage.domain.media.Media;

@Repository
public interface SourceRepository extends BaseRepository<Source> {
}
