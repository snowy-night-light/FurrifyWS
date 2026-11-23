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
package ws.furrify.storage.service.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import ws.furrify.storage.domain.source.SourceRepository;

import java.util.List;
import java.util.UUID;

/**
 * Fills {@code source.data_url} for sources saved before the column existed: lookups by URL (e.g. the
 * importer finding an image it already has) only see sources with it set. Runs in the background after
 * startup, walking the table by id; a no-op once everything is filled, and safe on several instances.
 */
@Slf4j
@Component
class SourceDataUrlBackfill {

    private static final int BATCH = 10_000;

    private final SourceRepository sourceRepository;

    SourceDataUrlBackfill(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    void start() {
        Thread.ofVirtual().name("source-data-url-backfill").start(this::run);
    }

    private void run() {
        try {
            if (!sourceRepository.existsWithoutDataUrl()) return;
            log.info("Filling source.data_url for older sources");
            UUID last = null;
            long filled = 0;
            while (true) {
                List<UUID> ids = sourceRepository.findIdsAfter(last, Limit.of(BATCH));
                if (ids.isEmpty()) break;
                filled += sourceRepository.fillDataUrl(ids);
                last = ids.getLast();
            }
            log.info("Filled source.data_url of {} sources", filled);
        } catch (RuntimeException e) {
            log.warn("Filling source.data_url failed (retried at the next start): {}", e.getMessage());
        }
    }
}
