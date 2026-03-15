package ws.furrify.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseEntityRepository<ENTITY extends BaseEntity> extends JpaRepository<ENTITY, UUID> {
    ENTITY getById(UUID id);
}
