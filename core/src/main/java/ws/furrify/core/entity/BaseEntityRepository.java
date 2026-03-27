package ws.furrify.core.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import java.util.Optional;
import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@NoRepositoryBean
public interface BaseEntityRepository<ENTITY extends BaseEntity> extends JpaRepository<ENTITY, UUID>, JpaSpecificationExecutor<ENTITY> {

    default Optional<ENTITY> findById(UUID id, EntitySpecResult<ENTITY> entitySpec) {
        return findOne(
                EntitySpec.<ENTITY>specBuilder().where("id", specEquals(id)).and(entitySpec).build().specification()
        );
    }

    default Page<ENTITY> findAll(Pageable pageable, EntitySpecResult<ENTITY> entitySpec) {
        return findAll(entitySpec.specification(), pageable);
    }

    default boolean existsById(UUID id, EntitySpecResult<ENTITY> entitySpec) {
        return exists(
                EntitySpec.<ENTITY>specBuilder()
                        .where("id", EntitySpec.specEquals(id))
                        .and(entitySpec)
                        .build()
                        .specification()
        );
    }

    default void deleteById(UUID id, EntitySpecResult<ENTITY> entitySpec) {
        findById(id, entitySpec).ifPresent(this::delete);
    }
}
