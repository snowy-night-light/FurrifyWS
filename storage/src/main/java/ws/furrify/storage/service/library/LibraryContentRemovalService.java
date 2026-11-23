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
package ws.furrify.storage.service.library;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.furrify.core.exception.ConflictException;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.storage.domain.library.LibraryRepository.RemovalState;
import ws.furrify.storage.service.library.LibraryRemovalBatchService.LeaseLostException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static ws.furrify.storage.service.library.LibraryRemovalBatchService.LEASE_TIMEOUT;

/**
 * Removes a library's content, or the library with everything in it, in the background: a big library has
 * millions of chapter versions, far too many to load as entities (JPA cascades) or to delete within one
 * request. Content goes in batches of {@link #BATCH}, each its own transaction in
 * {@link LibraryRemovalBatchService}; progress is logged as "[Library removal]".
 * <p>
 * What runs is recorded on the library ({@code scheduledForRemoval} for a deletion, {@code contentRemoval}
 * for a clear) with a lease ({@code removalHeartbeat}) the running instance refreshes every minute: clients
 * see it, a second request is refused with 409 meanwhile, and a removal whose instance stopped (a restart, a
 * crash, several instances) or that failed is taken over by any instance once its lease runs out. The lease
 * records its owner ({@code LAUNCH_ID} of the instance), and every batch checks it inside its own transaction
 * before deleting: two instances never remove from the same library at once, even if one only seemed gone.
 * Every batch commits on its own, so a removal picked up again simply carries on with what is left.
 * <p>
 * Media are never deleted here: they may be shared with other libraries and books. Media nothing uses any
 * more are removed later by {@code MediaOrphanCleanupService} (a week after a removal released them), and
 * generated book files by {@code BookFileCleanupService}. Sources of removed books, chapters, posts and
 * artists are removed with them; sources of media stay with the media.
 * <p>
 * Callers check the user's access to the library first; this service doesn't.
 */
@Slf4j
@Service
public class LibraryContentRemovalService {

    /** Entities removed per transaction: big enough to keep per-batch overhead low, small enough for short locks. */
    private static final int BATCH = 1000;

    private final LibraryRemovalBatchService batches;
    private final AsyncUtils asyncUtils;
    /** Libraries with a removal running in this instance. */
    private final Set<UUID> running = ConcurrentHashMap.newKeySet();

    public LibraryContentRemovalService(LibraryRemovalBatchService batches, AsyncUtils asyncUtils) {
        this.batches = batches;
        this.asyncUtils = asyncUtils;
    }

    /**
     * Remove {@code parts} of the library in the background; the library itself stays. Refused with 409
     * while the library is being deleted or some of its content is already being removed.
     */
    public void clearAsync(UUID libraryId, Set<LibraryContent> parts) {
        if (parts == null || parts.isEmpty()) return;
        Set<LibraryContent> what = EnumSet.copyOf(parts);
        // marker and lease in one statement: no other request or instance can start a removal in between
        if (!batches.markContentRemoval(libraryId, what)) {
            Optional<RemovalState> state = batches.state(libraryId);
            if (state.isPresent() && state.get().isScheduledForRemoval()) {
                throw new ConflictException("Library " + libraryId + " is being deleted.");
            }
            throw new ConflictException("Content of library " + libraryId + " is already being removed ("
                    + state.map(s -> String.join(", ", s.getContentRemoval() == null ? List.of() : s.getContentRemoval())).orElse("?")
                    + "); try again once that is done.");
        }
        run(libraryId, what, false);
    }

    /**
     * Remove the library with everything in it, in the background. The library is marked
     * {@code scheduledForRemoval} first, atomically. While a removal of it runs (on any instance), a deletion
     * already requested is refused with 409; a new one starts as soon as the running clear is done.
     */
    public void deleteAsync(UUID libraryId) {
        boolean alreadyMarked = !batches.markScheduledForRemoval(libraryId);
        if (batches.claim(libraryId)) {
            run(libraryId, Set.of(), true);
            return;
        }
        if (alreadyMarked) throw new ConflictException("Library " + libraryId + " is already being deleted.");
        // a clear of it runs (here or on another instance): its end starts the deletion
        log.info("[Library removal] {}: deletion starts once the running removal is done", libraryId);
    }

    public boolean isRunning(UUID libraryId) {
        return running.contains(libraryId);
    }

    /**
     * Every minute, on every instance: refresh the lease of the removals running here, and take over removals
     * whose lease ran out (their instance stopped, or the removal failed). The takeover claims the lease
     * atomically, so only one instance carries a removal on.
     */
    @Scheduled(initialDelay = 30 * 1000, fixedDelay = 60 * 1000)
    public void maintainLeases() {
        try {
            batches.refreshLeases(List.copyOf(running));
        } catch (RuntimeException e) {
            log.warn("[Library removal] Could not refresh removal leases: {}", e.getMessage());
        }
        List<RemovalState> unfinished;
        try {
            unfinished = batches.unfinishedRemovals();
        } catch (RuntimeException e) {
            log.warn("[Library removal] Could not look for unfinished removals: {}", e.getMessage());
            return;
        }
        for (RemovalState state : unfinished) {
            UUID libraryId = state.getId();
            if (running.contains(libraryId) || !batches.claim(libraryId)) continue;
            boolean deletion = state.isScheduledForRemoval();
            Set<LibraryContent> parts = deletion ? Set.of() : parseParts(state.getContentRemoval());
            log.info("[Library removal] {}: taking over the unfinished {}", libraryId, deletion ? "deletion" : "removal of " + parts);
            if (!deletion && parts.isEmpty()) {
                unmarkClear(libraryId);
                continue;
            }
            try {
                run(libraryId, parts, deletion);
            } catch (RuntimeException e) {
                log.warn("[Library removal] {}: could not take over: {}", libraryId, e.getMessage());
            }
        }
    }

    /** Run a removal whose lease this instance holds, on the shared executor. */
    private void run(UUID libraryId, Set<LibraryContent> parts, boolean deletion) {
        if (!running.add(libraryId)) {
            throw new ConflictException("Library " + libraryId + " is already busy with a removal; try again once that is done.");
        }
        String what = deletion ? "deleting the library" : "removing " + parts;
        asyncUtils.runAsyncNoTransaction(() -> {
            long started = System.currentTimeMillis();
            log.info("[Library removal] {}: {}...", libraryId, what);
            boolean done = false;
            try {
                for (LibraryContent part : deletion ? EnumSet.allOf(LibraryContent.class) : parts) remove(libraryId, part);
                if (deletion) batches.deleteLibrary(libraryId);
                done = true;
                log.info("[Library removal] {}: done {} in {} s", libraryId, what, (System.currentTimeMillis() - started) / 1000);
            } catch (LeaseLostException e) {
                log.info("[Library removal] {}: {}; stopping here", libraryId, e.getMessage());
                return;
            } catch (RuntimeException e) {
                // the marker stays: once the lease runs out, an instance tries again from where it stopped
                log.error("[Library removal] {}: {} stopped: {}; it is retried in about {} minutes",
                        libraryId, what, e.getMessage(), LEASE_TIMEOUT.toMinutes(), e);
            } finally {
                running.remove(libraryId);
            }
            if (!done || deletion) return;
            unmarkClear(libraryId);
            // the library was marked for deletion while this clear ran: delete it now
            try {
                if (batches.state(libraryId).map(RemovalState::isScheduledForRemoval).orElse(false) && batches.claim(libraryId)) {
                    run(libraryId, Set.of(), true);
                }
            } catch (RuntimeException e) {
                log.warn("[Library removal] {}: the deletion requested meanwhile could not start: {} (taken over within {} minutes)",
                        libraryId, e.getMessage(), LEASE_TIMEOUT.toMinutes());
            }
        });
    }

    private void unmarkClear(UUID libraryId) {
        try {
            batches.unmarkContentRemoval(libraryId);
        } catch (RuntimeException e) {
            log.warn("[Library removal] {}: could not clear the removal marker: {}", libraryId, e.getMessage());
        }
    }

    private static Set<LibraryContent> parseParts(List<String> recorded) {
        Set<LibraryContent> parts = EnumSet.noneOf(LibraryContent.class);
        if (recorded == null) return parts;
        for (String name : recorded) {
            try {
                parts.add(LibraryContent.valueOf(name.trim()));
            } catch (IllegalArgumentException ignored) {
                // a part this version doesn't know: skipped
            }
        }
        return parts;
    }

    private void remove(UUID libraryId, LibraryContent part) {
        switch (part) {
            case BOOKS -> inBatches(libraryId, "books", batches::removeBooks);
            case POSTS -> inBatches(libraryId, "posts", batches::removePosts);
            case COLLECTIONS -> log.info("[Library removal] {}: {} collections removed", libraryId, batches.removeCollections(libraryId));
            case TAGS -> {
                inBatches(libraryId, "tags", batches::removeTags);
                log.info("[Library removal] {}: {} tag categories removed", libraryId, batches.removeTagCategories(libraryId));
            }
            case ARTISTS -> inBatches(libraryId, "artists", batches::removeArtists);
        }
    }

    /** Call {@code batch} until it removes nothing more, logging the progress. */
    private void inBatches(UUID libraryId, String what, BiFunction<UUID, Integer, Integer> batch) {
        long removed = 0, lastLogged = 0, started = System.currentTimeMillis();
        int count;
        while ((count = batch.apply(libraryId, BATCH)) > 0) {
            removed += count;
            if (removed - lastLogged >= 10_000) {
                long seconds = Math.max(1, (System.currentTimeMillis() - started) / 1000);
                log.info("[Library removal] {}: {} {} removed so far ({} per minute)", libraryId, removed, what, removed * 60 / seconds);
                lastLogged = removed;
            }
        }
        log.info("[Library removal] {}: {} {} removed", libraryId, removed, what);
    }
}
