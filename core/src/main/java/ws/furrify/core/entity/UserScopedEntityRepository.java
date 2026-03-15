package ws.furrify.core.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface UserScopedEntityRepository<ENTITY extends UserScopedEntity> extends BaseEntityRepository<ENTITY> {
    Optional<ENTITY> findByIdAndOwnerId(UUID id, UUID ownerId);
    Page<ENTITY> findAllByOwnerId(UUID ownerId, Pageable pageable);
    void deleteByIdAndOwnerId(UUID id, UUID ownerId);
}
