package ws.furrify.storage.domain.artist;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseRepository;
import ws.furrify.storage.domain.media.Media;

@Repository
public interface ArtistRepository extends BaseRepository<Artist> {
}
