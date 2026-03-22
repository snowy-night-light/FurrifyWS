package ws.furrify.core.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseEntityRepository<ENTITY extends BaseEntity> extends JpaRepository<ENTITY, UUID>, JpaSpecificationExecutor<ENTITY> {

    default Optional<ENTITY> findById(UUID id, Specification<ENTITY> spec) {
        return findOne(
                Specification.where(spec)
                        .and((root, query, cb) -> cb.equal(root.get("id"), id))
        );
    }

    default Page<ENTITY> findAll(Pageable pageable, Specification<ENTITY> spec) {
        return findAll(spec, pageable);
    }

    default void deleteById(UUID id, Specification<ENTITY> spec) {
        findById(id, spec).ifPresent(this::delete);
    }
}
