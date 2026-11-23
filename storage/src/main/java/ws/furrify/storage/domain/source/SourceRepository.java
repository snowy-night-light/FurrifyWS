/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.domain.source;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SourceRepository extends BaseEntityRepository<Source> {

    @Query("SELECT count(s) > 0 FROM Source s WHERE s.dataUrl IS NULL AND json_value(s.data, '$.url') IS NOT NULL")
    boolean existsWithoutDataUrl();

    /** Source ids after {@code after} (null: from the start), in id order: walks the table in batches. */
    @Query("SELECT s.id FROM Source s WHERE :after IS NULL OR s.id > :after ORDER BY s.id")
    List<UUID> findIdsAfter(@Param("after") UUID after, Limit limit);

    /** Copy {@code data.url} into {@code dataUrl} where it is missing. */
    @Transactional
    @Modifying
    @Query("UPDATE Source s SET s.dataUrl = json_value(s.data, '$.url')"
            + " WHERE s.id IN :ids AND s.dataUrl IS NULL AND json_value(s.data, '$.url') IS NOT NULL")
    int fillDataUrl(@Param("ids") Collection<UUID> ids);

    /** Sources of removed entities (their links are gone already). */
    @Modifying
    @Query("DELETE FROM Source s WHERE s.id IN :sources")
    int deleteByIds(@Param("sources") List<UUID> sources);
}
