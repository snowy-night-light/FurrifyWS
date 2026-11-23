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
package ws.furrify.storage.domain.library;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LibraryRepository extends BaseEntityRepository<Library> {

    /** Mark a clear of {@code parts} and take its lease, unless the library is being deleted or cleared already. */
    @Modifying
    @Query("UPDATE Library l SET l.contentRemoval = :parts, l.removalHeartbeat = :now, l.removalOwner = :owner"
            + " WHERE l.id = :library AND l.contentRemoval IS NULL AND l.scheduledForRemoval = false")
    int markContentRemoval(@Param("library") UUID library, @Param("parts") List<String> parts,
                           @Param("now") ZonedDateTime now, @Param("owner") UUID owner);

    @Modifying
    @Query("UPDATE Library l SET l.scheduledForRemoval = true WHERE l.id = :library AND l.scheduledForRemoval = false")
    int markScheduledForRemoval(@Param("library") UUID library);

    /** Take the removal lease when nobody holds it, or its holder stopped refreshing it before {@code stale}. */
    @Modifying
    @Query("UPDATE Library l SET l.removalHeartbeat = :now, l.removalOwner = :owner"
            + " WHERE l.id = :library AND (l.removalHeartbeat IS NULL OR l.removalHeartbeat < :stale)")
    int claimRemoval(@Param("library") UUID library, @Param("now") ZonedDateTime now,
                     @Param("stale") ZonedDateTime stale, @Param("owner") UUID owner);

    /** Refresh the leases {@code owner} holds; also locks the rows, so a takeover waits for the transaction. */
    @Modifying
    @Query("UPDATE Library l SET l.removalHeartbeat = :now WHERE l.id IN :libraries AND l.removalOwner = :owner")
    int refreshRemovalLeases(@Param("libraries") List<UUID> libraries, @Param("now") ZonedDateTime now,
                             @Param("owner") UUID owner);

    /** A finished clear: no marker, no lease (only by the lease's holder). */
    @Modifying
    @Query("UPDATE Library l SET l.contentRemoval = NULL, l.removalHeartbeat = NULL, l.removalOwner = NULL"
            + " WHERE l.id = :library AND l.removalOwner = :owner")
    int unmarkContentRemoval(@Param("library") UUID library, @Param("owner") UUID owner);

    @Query("SELECT l.id AS id, l.scheduledForRemoval AS scheduledForRemoval, l.contentRemoval AS contentRemoval"
            + " FROM Library l WHERE l.id = :library")
    Optional<LibraryRemovalState> findRemovalState(@Param("library") UUID library);

    /** Removals whose lease nobody refreshed since {@code stale}. */
    @Query("SELECT l.id AS id, l.scheduledForRemoval AS scheduledForRemoval, l.contentRemoval AS contentRemoval FROM Library l"
            + " WHERE (l.scheduledForRemoval = true OR l.contentRemoval IS NOT NULL)"
            + " AND (l.removalHeartbeat IS NULL OR l.removalHeartbeat < :stale)")
    List<LibraryRemovalState> findUnfinishedRemovals(@Param("stale") ZonedDateTime stale);

    /** The library row only: its content is removed first. */
    @Modifying
    @Query("DELETE FROM Library l WHERE l.id = :library")
    int deleteLibrary(@Param("library") UUID library);

    @Query("SELECT (SELECT count(b) FROM Book b WHERE b.library.id = l.id) AS books,"
            + " (SELECT count(c) FROM BookChapter c WHERE c.book.library.id = l.id) AS chapters,"
            + " (SELECT count(p) FROM Post p WHERE p.library.id = l.id) AS posts,"
            + " (SELECT count(c) FROM Collection c WHERE c.library.id = l.id) AS collections,"
            + " (SELECT count(t) FROM Tag t WHERE t.library.id = l.id) AS tags,"
            + " (SELECT count(t) FROM TagCategory t WHERE t.library.id = l.id) AS tagCategories,"
            + " (SELECT count(a) FROM Artist a WHERE a.library.id = l.id) AS artists"
            + " FROM Library l WHERE l.id = :library")
    Optional<LibraryContentCounts> countContent(@Param("library") UUID library);
}
