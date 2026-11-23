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
package ws.furrify.storage.service.media;

import org.springframework.data.domain.Limit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.furrify.storage.service.lock.JobLockServiceImpl;
import ws.furrify.storage.domain.media.MediaRepository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

/**
 * Media are never cascade-deleted (a cover, an avatar and chapter images may be the same media).
 * Instead this job periodically removes media that nothing references any more; their files are
 * then removed by the media entity listener when no other media uses them.
 * <p>
 * Only media older than {@link #GRACE} are considered, so a media created just before the entity
 * that uses it (e.g. a cover uploaded before its book is saved) is never caught in between.
 */
@Slf4j
@Service
public class MediaOrphanCleanupService {

    static final Duration GRACE = Duration.ofHours(24);
    /**
     * Media a library removal unlinked ({@code released_at}) are kept this long, so the library can be
     * imported again (a full FimFarchive import takes a day or more) and finds them by their source URL.
     */
    static final Duration RELEASED_GRACE = Duration.ofDays(7);
    private static final int BATCH = 500;
    private static final int MAX_BATCHES_PER_RUN = 200;

    /** One instance cleans at a time; the lease outlasts a run and runs out by itself if the instance stops. */
    private static final String JOB = "media-orphan-cleanup";
    private static final Duration LEASE = Duration.ofHours(1);

    private final MediaRepository mediaRepository;
    private final JobLockServiceImpl jobLocks;

    private final boolean enabled;

    /**
     * {@code furrify.media.orphan-cleanup.enabled=false} pauses the cleanup, e.g. while books are deleted to be
     * imported again: their media are unreferenced until the import uses them again (it finds them by their
     * source URL), and would otherwise be deleted, files included, and downloaded again.
     */
    public MediaOrphanCleanupService(MediaRepository mediaRepository, JobLockServiceImpl jobLocks,
                                     @Value("${furrify.media.orphan-cleanup.enabled:true}") boolean enabled) {
        this.mediaRepository = mediaRepository;
        this.jobLocks = jobLocks;
        this.enabled = enabled;
        if (!enabled) log.warn("Orphaned media cleanup is paused (furrify.media.orphan-cleanup.enabled=false)");
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    public void removeOrphanedMedia() {
        if (!enabled) return;
        jobLocks.runExclusively(JOB, LEASE, this::removeOrphanedMediaNow); // skipped while another instance cleans
    }

    private void removeOrphanedMediaNow() {
        ZonedDateTime createdBefore = ZonedDateTime.now().minus(GRACE);
        ZonedDateTime releasedBefore = ZonedDateTime.now().minus(RELEASED_GRACE);
        long removed = 0;
        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
            List<UUID> ids;
            try {
                ids = mediaRepository.findOrphanIds(createdBefore, releasedBefore, Limit.of(BATCH));
                if (ids.isEmpty()) break;
                // entity deletes (one transaction): cascade to the media's sources and fire the listener (file removal)
                mediaRepository.deleteAllById(ids);
            } catch (RuntimeException e) {
                // e.g. a media got linked between the lookup and the delete (FK violation): next run retries
                log.warn("Orphaned media cleanup batch failed: {}", e.getMessage());
                break;
            }
            removed += ids.size();
            if (ids.size() < BATCH) break;
        }
        if (removed > 0) log.info("Removed {} orphaned media", removed);
    }
}
