/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.furrify.core.lock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/** Repository of a service's {@link JobLock} entity: {@code interface JobLockImplRepository extends JobLockRepository<JobLockImpl>}. */
@NoRepositoryBean
public interface JobLockRepository<LOCK extends JobLock> extends JpaRepository<LOCK, String> {

    /** Take the lease until {@code until}: when it is free, ran out before {@code now}, or is {@code owner}'s already. */
    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} l SET l.lockedUntil = :until, l.owner = :owner WHERE l.name = :name"
            + " AND (l.lockedUntil IS NULL OR l.lockedUntil < :now OR l.owner = :owner)")
    int acquire(@Param("name") String name, @Param("owner") UUID owner,
                @Param("now") ZonedDateTime now, @Param("until") ZonedDateTime until);

    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} l SET l.lockedUntil = NULL, l.owner = NULL WHERE l.name = :name AND l.owner = :owner")
    int release(@Param("name") String name, @Param("owner") UUID owner);
}
