/*
 * furrify-core - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.core.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseEntityRepository<ENTITY extends BaseEntity> extends JpaRepository<ENTITY, UUID>, JpaSpecificationExecutor<ENTITY> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<ENTITY> findByIdWithPessimisticLock(@Param("id") UUID id);

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
