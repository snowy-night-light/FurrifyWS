package ws.furrify.storage.domain.book;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

@Repository
public interface BookRepository extends BaseEntityRepository<Book> {
}
