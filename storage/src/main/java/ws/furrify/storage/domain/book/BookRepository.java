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
package ws.furrify.storage.domain.book;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends BaseEntityRepository<Book> {

    /** Which of these attachments are a book's generated file (any book, any user). */
    @Query("SELECT DISTINCT VALUE(f) FROM Book b JOIN b.formatReferenceIds f WHERE VALUE(f) IN :attachmentIds")
    List<UUID> findReferencedFormatFiles(@org.springframework.data.repository.query.Param("attachmentIds") Collection<UUID> attachmentIds);

    interface BookActiveTaskProjection {
        UUID getId();
        UUID getOwnerId();
        UUID getActiveWorkerTaskId();
        Integer getGenerationRetryCount();
    }

    @Query("SELECT b.id FROM Book b WHERE b.needsBookFileGeneration = true AND (b.fileGenerationFailed = false OR b.fileGenerationFailed IS NULL)")
    List<UUID> findBookIdsNeedingGeneration(Pageable pageable);

    @Query("SELECT b.id AS id, b.ownerId AS ownerId, b.activeWorkerTaskId AS activeWorkerTaskId, b.generationRetryCount AS generationRetryCount FROM Book b WHERE b.activeWorkerTaskId IS NOT NULL")
    List<BookActiveTaskProjection> findBooksWithActiveWorkerTask(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.needsBookFileGeneration = false WHERE b.id = :bookId AND b.needsBookFileGeneration = true")
    int claimBookForGeneration(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.generationRetryCount = COALESCE(b.generationRetryCount, 0) + 1 WHERE b.id = :bookId")
    int incrementGenerationRetryCount(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.generationRetryCount = 0, b.fileGenerationFailed = false WHERE b.id = :bookId")
    int resetGenerationRetryCount(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.fileGenerationFailed = true, b.activeWorkerTaskId = null, b.needsBookFileGeneration = false WHERE b.id = :bookId")
    int markFileGenerationFailed(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.totalWordCount = :wordCount, b.needsBookFileGeneration = true, b.fileGenerationFailed = false, b.generationRetryCount = 0 WHERE b.id = :bookId")
    int updateTotalWordCountAndMarkNeedsGeneration(@Param("bookId") UUID bookId, @Param("wordCount") long wordCount);

    /**
     * Recompute the denormalised chapter count. A bulk update, so it writes the column even though entity
     * saves don't ({@code updatable = false}); COUNT is a Long, the column an Integer, hence the cast.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.chaptersCount = CAST((SELECT COUNT(c) FROM BookChapter c WHERE c.book.id = :bookId) AS Integer)"
            + " WHERE b.id = :bookId")
    int refreshChaptersCount(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.needsBookFileGeneration = :needsGeneration, b.fileGenerationFailed = CASE WHEN :needsGeneration = true THEN false ELSE b.fileGenerationFailed END, b.generationRetryCount = CASE WHEN :needsGeneration = true THEN 0 ELSE b.generationRetryCount END WHERE b.id = :bookId AND (b.needsBookFileGeneration IS NULL OR b.needsBookFileGeneration <> :needsGeneration)")
    int updateNeedsBookFileGeneration(@Param("bookId") UUID bookId, @Param("needsGeneration") boolean needsGeneration);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.activeWorkerTaskId = null WHERE b.id = :bookId AND b.activeWorkerTaskId IS NOT NULL")
    int clearActiveWorkerTaskId(@Param("bookId") UUID bookId);
    
    @Query("SELECT b.id FROM Book b WHERE b.library.id = :library")
    List<UUID> findIdsByLibrary(@Param("library") UUID library, Limit limit);

    @Query("SELECT b.id AS id, b.ownerId AS ownerId, b.activeWorkerTaskId AS activeWorkerTaskId, b.generationRetryCount AS generationRetryCount"
            + " FROM Book b WHERE b.id IN :books AND b.activeWorkerTaskId IS NOT NULL")
    List<BookActiveTaskProjection> findActiveWorkerTasks(@Param("books") List<UUID> books);

    /** Sources of the books and of their chapters. */
    @Query("SELECT s.id FROM Book b JOIN b.sources s WHERE b.id IN :books"
            + " UNION SELECT s.id FROM BookChapter c JOIN c.sources s WHERE c.book.id IN :books")
    List<UUID> findSourceIdsOfBooks(@Param("books") List<UUID> books);

    /** The books; Hibernate removes their rows in their own join and collection tables (tags, artists, sources, ...) first. */
    @Modifying
    @Query("DELETE FROM Book b WHERE b.id IN :books")
    int deleteByIds(@Param("books") List<UUID> books);

    /** Books linked to any of these tags (to unlink them before the tags go). */
    @Query("SELECT DISTINCT b FROM Book b JOIN b.tags t WHERE t.id IN :tags")
    List<Book> findLinkedToTags(@Param("tags") List<UUID> tags);

    /** Books linked to any of these artists (to unlink them before the artists go). */
    @Query("SELECT DISTINCT b FROM Book b JOIN b.artists a WHERE a.id IN :artists")
    List<Book> findLinkedToArtists(@Param("artists") List<UUID> artists);
}
