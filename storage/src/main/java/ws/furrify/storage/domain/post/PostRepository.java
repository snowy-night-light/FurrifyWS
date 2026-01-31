package ws.furrify.storage.domain.post;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseRepository;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;

@Repository
public interface PostRepository extends BaseRepository<Post> {

}
