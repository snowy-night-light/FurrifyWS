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
        if (entitySpec == null || entitySpec.specString().isEmpty()) {
            return findById(id);
        }
        
        // Bypass Hibernate 6 Criteria API EAGER element collection inner-join bug
        // by verifying security rules via exists(), then fetching normally via standard findById(id).
        if (!existsById(id, entitySpec)) {
            return Optional.empty();
        }
        return findById(id);
    }

    default long count(EntitySpecResult<ENTITY> entitySpec) {
        return count(entitySpec.specification());
    }

    default Page<ENTITY> findAll(Pageable pageable, EntitySpecResult<ENTITY> entitySpec) {
        return findAll(entitySpec.specification(), pageable);
    }

    default boolean existsById(UUID id, EntitySpecResult<ENTITY> entitySpec) {
        if (entitySpec == null || entitySpec.specString().isEmpty()) {
            return existsById(id);
        }
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
