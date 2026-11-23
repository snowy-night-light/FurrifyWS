/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.domain.worker.plugin;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.worker.domain.worker.WorkStatus;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface PluginImportUserWorkerTaskRepository extends BaseEntityRepository<PluginImportUserWorkerTask> {

    /**
     * Whether another import into {@code libraryId} with {@code status} started before this one
     * (earlier startedAt, ties broken by id). Used so only one import per library runs at a time,
     * also across worker instances; the deterministic tie-break means exactly one of two
     * simultaneous starters wins.
     */
    @Query("SELECT COUNT(t) > 0 FROM PluginImportUserWorkerTask t"
            + " WHERE t.destinationLibraryReferenceId = :libraryId AND t.status = :status AND t.id <> :taskId"
            + " AND (t.startedAt < :startedAt OR (t.startedAt = :startedAt AND t.id < :taskId))")
    boolean existsEarlierImportIntoLibrary(@Param("libraryId") UUID libraryId, @Param("status") WorkStatus status,
                                           @Param("taskId") UUID taskId, @Param("startedAt") ZonedDateTime startedAt);

    /**
     * Ended tasks (before {@code before}) whose archive is still stored and that no waiting or
     * running task shares.
     */
    @Query("SELECT t FROM PluginImportUserWorkerTask t"
            + " WHERE t.status IN :ended AND t.fileRemoved = false AND t.modifiedAt < :before"
            + " AND NOT EXISTS (SELECT o.id FROM PluginImportUserWorkerTask o"
            + "   WHERE o.fileReferenceId = t.fileReferenceId AND o.status IN :active)"
            + " ORDER BY t.modifiedAt")
    List<PluginImportUserWorkerTask> findWithRemovableArchive(@Param("ended") Collection<WorkStatus> ended,
                                                                        @Param("active") Collection<WorkStatus> active,
                                                                        @Param("before") ZonedDateTime before,
                                                                        Pageable pageable);

    @Modifying
    @Query("UPDATE PluginImportUserWorkerTask t SET t.fileRemoved = true WHERE t.fileReferenceId = :fileReferenceId")
    int markFileRemoved(@Param("fileReferenceId") UUID fileReferenceId);
}
