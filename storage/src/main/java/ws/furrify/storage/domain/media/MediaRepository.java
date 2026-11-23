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
package ws.furrify.storage.domain.media;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface MediaRepository extends BaseEntityRepository<Media> {

    /**
     * Media created before {@code createdBefore} that nothing references. Every relation that
     * points at a media must be listed here, otherwise its media get deleted while in use.
     * (the media's own sources are not a usage.)
     */
    @Query("SELECT m.id FROM Media m WHERE m.createdAt < :createdBefore"
            + " AND (m.releasedAt IS NULL OR m.releasedAt < :releasedBefore)"
            + " AND NOT EXISTS (SELECT 1 FROM Book b WHERE b.cover = m)"
            + " AND NOT EXISTS (SELECT 1 FROM Artist a WHERE a.avatar = m)"
            + " AND NOT EXISTS (SELECT 1 FROM Post p JOIN p.displayMediaList d WHERE d = m)"
            + " AND NOT EXISTS (SELECT 1 FROM Post p JOIN p.attachments at WHERE at = m)"
            + " AND NOT EXISTS (SELECT 1 FROM BookChapterVersion v JOIN v.media vm WHERE vm = m)")
    List<UUID> findOrphanIds(@Param("createdBefore") ZonedDateTime createdBefore,
                             @Param("releasedBefore") ZonedDateTime releasedBefore, Limit limit);

    /** Whether any media still uses the attachment {@code fileReferenceId} (files are shared between media). */
    boolean existsByFileReferenceId(UUID fileReferenceId);

    /** Cluster-wide guard so only one storage instance runs the cleanup at a time; released at transaction end. */
    @Query(value = "SELECT pg_try_advisory_xact_lock(hashtext('media-orphan-cleanup'))", nativeQuery = true)
    boolean tryCleanupLock();

    // ------------------------------------------------------------------ library removal: media stay, released (Media.releasedAt)

    /** Covers of the books and images of their chapters: their orphan grace period starts now. */
    @Modifying
    @Query("UPDATE Media m SET m.releasedAt = :now WHERE m.id IN (SELECT b.cover.id FROM Book b WHERE b.id IN :books)"
            + " OR m.id IN (SELECT vm.id FROM BookChapterVersion v JOIN v.media vm WHERE v.chapter.book.id IN :books)")
    int releaseMediaOfBooks(@Param("books") List<UUID> books, @Param("now") ZonedDateTime now);

    @Modifying
    @Query("UPDATE Media m SET m.releasedAt = :now WHERE m.id IN (SELECT dm.id FROM Post p JOIN p.displayMediaList dm WHERE p.id IN :posts)"
            + " OR m.id IN (SELECT am.id FROM Post p JOIN p.attachments am WHERE p.id IN :posts)")
    int releaseMediaOfPosts(@Param("posts") List<UUID> posts, @Param("now") ZonedDateTime now);

    @Modifying
    @Query("UPDATE Media m SET m.releasedAt = :now WHERE m.id IN (SELECT a.avatar.id FROM Artist a WHERE a.id IN :artists)")
    int releaseAvatarsOfArtists(@Param("artists") List<UUID> artists, @Param("now") ZonedDateTime now);
}
