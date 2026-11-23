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

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.book.BookRepository;
import ws.furrify.storage.domain.book.BookRepository.BookActiveTaskProjection;
import ws.furrify.storage.domain.book.chapter.BookChapterRepository;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersionRepository;
import ws.furrify.storage.domain.collection.CollectionRepository;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.library.LibraryRepository.RemovalState;
import ws.furrify.storage.domain.media.MediaRepository;
import ws.furrify.storage.domain.post.PostRepository;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.domain.tag.alias.TagAliasRepository;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.service.book.BookFileGenerationService;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The transactions of a library removal, one per call (through this bean's proxy), for
 * {@link LibraryContentRemovalService}: markers, the lease, and the batches that remove content. Every batch
 * first confirms this instance still holds the lease ({@link #fence}), in its own transaction, so two
 * instances never remove from the same library at once.
 */
@Service
public class LibraryRemovalBatchService {

    /** A removal whose lease wasn't refreshed for this long has no instance working on it any more. */
    static final Duration LEASE_TIMEOUT = Duration.ofMinutes(5);
    /** This instance (one run of storage): the owner recorded with the leases it holds. */
    static final UUID ME = StorageApplication.LAUNCH_ID;
    private static final int SOURCE_CHUNK = 1000;

    private final LibraryRepository libraries;
    private final BookRepository books;
    private final BookChapterRepository chapters;
    private final BookChapterVersionRepository versions;
    private final PostRepository posts;
    private final CollectionRepository collections;
    private final TagRepository tags;
    private final TagAliasRepository tagAliases;
    private final TagCategoryRepository tagCategories;
    private final ArtistRepository artists;
    private final SourceRepository sources;
    private final MediaRepository media;
    private final AsyncUtils asyncUtils;
    private final BookFileGenerationService bookFileGeneration;

    public LibraryRemovalBatchService(LibraryRepository libraries, BookRepository books, BookChapterRepository chapters,
                                      BookChapterVersionRepository versions, PostRepository posts,
                                      CollectionRepository collections, TagRepository tags, TagAliasRepository tagAliases,
                                      TagCategoryRepository tagCategories, ArtistRepository artists,
                                      SourceRepository sources, MediaRepository media, AsyncUtils asyncUtils,
                                      @Lazy BookFileGenerationService bookFileGeneration) {
        this.libraries = libraries;
        this.books = books;
        this.chapters = chapters;
        this.versions = versions;
        this.posts = posts;
        this.collections = collections;
        this.tags = tags;
        this.tagAliases = tagAliases;
        this.tagCategories = tagCategories;
        this.artists = artists;
        this.sources = sources;
        this.media = media;
        this.asyncUtils = asyncUtils;
        this.bookFileGeneration = bookFileGeneration;
    }

    /** Thrown inside a batch when another instance holds the library's removal lease now: the batch rolls back. */
    static final class LeaseLostException extends RuntimeException {
        LeaseLostException(UUID libraryId) {
            super("another storage instance took over the removal of library " + libraryId);
        }
    }

    /** Mark a clear of {@code parts} and take its lease; false when the library is being deleted or cleared. */
    @Transactional
    public boolean markContentRemoval(UUID libraryId, Collection<LibraryContent> parts) {
        List<String> names = parts.stream().map(Enum::name).sorted().toList();
        return libraries.markContentRemoval(libraryId, names, ZonedDateTime.now(), ME) > 0;
    }

    /** Mark the library for deletion; false when it was marked already. */
    @Transactional
    public boolean markScheduledForRemoval(UUID libraryId) {
        return libraries.markScheduledForRemoval(libraryId) > 0;
    }

    /** Take the library's removal lease for this instance: true when nobody holds it (or it ran out). */
    @Transactional
    public boolean claim(UUID libraryId) {
        ZonedDateTime now = ZonedDateTime.now();
        return libraries.claimRemoval(libraryId, now, now.minus(LEASE_TIMEOUT), ME) > 0;
    }

    @Transactional
    public void refreshLeases(Collection<UUID> libraryIds) {
        if (!libraryIds.isEmpty()) libraries.refreshRemovalLeases(List.copyOf(libraryIds), ZonedDateTime.now(), ME);
    }

    /** A finished clear: no marker, no lease. */
    @Transactional
    public void unmarkContentRemoval(UUID libraryId) {
        libraries.unmarkContentRemoval(libraryId, ME);
    }

    @Transactional(readOnly = true)
    public Optional<RemovalState> state(UUID libraryId) {
        return libraries.findRemovalState(libraryId);
    }

    /** Removals nobody works on: their lease ran out. */
    @Transactional(readOnly = true)
    public List<RemovalState> unfinishedRemovals() {
        return libraries.findUnfinishedRemovals(ZonedDateTime.now().minus(LEASE_TIMEOUT));
    }

    /**
     * Before a batch deletes anything: confirm this instance still holds the lease (and refresh it). The
     * update locks the library row, so a takeover can't happen in the middle of the batch.
     */
    private void fence(UUID libraryId) {
        if (libraries.refreshRemovalLeases(List.of(libraryId), ZonedDateTime.now(), ME) == 0) {
            throw new LeaseLostException(libraryId);
        }
    }

    @Transactional
    public int removeBooks(UUID libraryId, int batch) {
        return removeBatch(libraryId, books.findIdsByLibrary(libraryId, Limit.of(batch)), ids -> {
            // their file generation (scheduled here, or a task at the worker) stops once the books are gone
            Map<UUID, List<UUID>> tasksByOwner = new HashMap<>();
            for (BookActiveTaskProjection task : books.findActiveWorkerTasks(ids)) {
                tasksByOwner.computeIfAbsent(task.getOwnerId(), k -> new ArrayList<>()).add(task.getActiveWorkerTaskId());
            }
            List<UUID> removed = List.copyOf(ids);
            asyncUtils.runAfterCommitNoTransaction(() -> bookFileGeneration.forgetRemovedBooks(removed, tasksByOwner));

            Set<UUID> sourceIds = new HashSet<>(books.findSourceIdsOfBooks(ids));
            media.releaseMediaOfBooks(ids, ZonedDateTime.now());
            // each bulk delete takes the entity's own join and collection tables with it (Hibernate)
            versions.deleteByBooks(ids);
            chapters.deleteByBooks(ids);
            books.deleteByIds(ids);
            deleteSources(sourceIds);
        });
    }

    @Transactional
    public int removePosts(UUID libraryId, int batch) {
        return removeBatch(libraryId, posts.findIdsByLibrary(libraryId, Limit.of(batch)), ids -> {
            Set<UUID> sourceIds = new HashSet<>(posts.findSourceIdsOfPosts(ids));
            media.releaseMediaOfPosts(ids, ZonedDateTime.now());
            collections.deleteLinksToPosts(ids);
            posts.deleteByIds(ids);
            deleteSources(sourceIds);
        });
    }

    @Transactional
    public int removeArtists(UUID libraryId, int batch) {
        return removeBatch(libraryId, artists.findIdsByLibrary(libraryId, Limit.of(batch)), ids -> {
            Set<UUID> sourceIds = new HashSet<>(artists.findSourceIdsOfArtists(ids));
            media.releaseAvatarsOfArtists(ids, ZonedDateTime.now());
            books.deleteLinksToArtists(ids);
            posts.deleteLinksToArtists(ids);
            artists.deleteByIds(ids);
            deleteSources(sourceIds);
        });
    }

    @Transactional
    public int removeTags(UUID libraryId, int batch) {
        return removeBatch(libraryId, tags.findIdsByLibrary(libraryId, Limit.of(batch)), ids -> {
            books.deleteLinksToTags(ids);
            posts.deleteLinksToTags(ids);
            tagAliases.deleteByTargetTags(ids);
            tags.deleteByIds(ids);
        });
    }

    /** The library's tag categories; its tags are removed first ({@link #removeTags}). */
    @Transactional
    public int removeTagCategories(UUID libraryId) {
        fence(libraryId);
        return tagCategories.deleteByLibrary(libraryId);
    }

    @Transactional
    public int removeCollections(UUID libraryId) {
        fence(libraryId);
        return collections.deleteByLibrary(libraryId);
    }

    /** The library itself, once its content is gone. */
    @Transactional
    public void deleteLibrary(UUID libraryId) {
        fence(libraryId);
        libraries.deleteLibrary(libraryId);
    }

    private int removeBatch(UUID libraryId, List<UUID> ids, Consumer<List<UUID>> removal) {
        if (ids.isEmpty()) return 0;
        fence(libraryId);
        removal.accept(ids);
        return ids.size();
    }

    private void deleteSources(Set<UUID> sourceIds) {
        List<UUID> all = List.copyOf(sourceIds);
        for (int i = 0; i < all.size(); i += SOURCE_CHUNK) {
            sources.deleteByIds(all.subList(i, Math.min(i + SOURCE_CHUNK, all.size())));
        }
    }
}
