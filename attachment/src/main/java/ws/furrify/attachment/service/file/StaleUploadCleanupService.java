/*
 * furrify-attachment-service - Furrify Workspace Project
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
package ws.furrify.attachment.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.furrify.attachment.domain.file.AttachmentFileRepository;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * An upload's record is saved once the file is received and hashed, then marked UPLOADED once the
 * file is stored. A failed upload deletes its record, but one whose request was killed in between
 * (a restart, a crash) stays NOT_UPLOADED forever. This job removes those records, and whatever
 * part of their file reached storage, once they are older than {@code furrify.attachment.stale-upload-hours}
 * (default 24, far longer than any upload takes).
 */
@Slf4j
@Service
public class StaleUploadCleanupService {

    private static final int BATCH = 200;
    private static final int MAX_BATCHES_PER_RUN = 50;

    private final AttachmentFileRepository repository;
    private final FileMassStorageStrategy storage;
    private final Duration grace;

    public StaleUploadCleanupService(AttachmentFileRepository repository, FileMassStorageStrategy storage,
                                     @Value("${furrify.attachment.stale-upload-hours:24}") long graceHours) {
        this.repository = repository;
        this.storage = storage;
        this.grace = Duration.ofHours(graceHours);
    }

    @Scheduled(initialDelay = 15 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    public void removeStaleUploads() {
        ZonedDateTime createdBefore = ZonedDateTime.now().minus(grace);
        long removed = 0;
        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
            List<UUID> ids;
            try {
                ids = repository.findStaleUploadIds(createdBefore, PageRequest.of(0, BATCH));
            } catch (RuntimeException e) {
                log.warn("Stale upload cleanup: lookup failed: {}", e.getMessage());
                return;
            }
            if (ids == null || ids.isEmpty()) break;
            for (UUID id : ids) {
                try {
                    // the record first (deleteById commits on its own): if the directory removal fails, nothing points at it any more
                    repository.deleteById(id);
                    storage.removeFileDirectory(id);
                    removed++;
                } catch (RuntimeException e) {
                    log.warn("Stale upload cleanup: could not remove upload {}: {}", id, e.getMessage());
                }
            }
            if (ids.size() < BATCH) break;
        }
        if (removed > 0) log.info("Removed {} uploads that never finished (older than {} h)", removed, grace.toHours());
    }
}
