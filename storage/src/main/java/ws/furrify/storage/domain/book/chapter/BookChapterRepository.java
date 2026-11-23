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
package ws.furrify.storage.domain.book.chapter;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookChapterRepository extends BaseEntityRepository<BookChapter> {

    @Query("SELECT COALESCE(SUM(c.currentNumberOfWords), 0) FROM BookChapter c WHERE c.book.id = :bookId")
    long sumWordsByBookId(@Param("bookId") UUID bookId);

    @Query("SELECT COUNT(c) > 0 FROM BookChapter c WHERE c.book.id = :bookId AND c.chapterNumber = :chapterNumber AND (:existingChapterId IS NULL OR c.id <> :existingChapterId)")
    boolean existsDuplicateChapterNumber(@Param("bookId") UUID bookId, @Param("chapterNumber") int chapterNumber, @Param("existingChapterId") UUID existingChapterId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookChapter c SET c.currentNumberOfWords = :wordCount WHERE c.id = :chapterId")
    void updateCurrentNumberOfWords(@Param("chapterId") UUID chapterId, @Param("wordCount") long wordCount);

    /**
     * Recompute the denormalised version count. A bulk update, so it writes the column even though entity
     * saves don't ({@code updatable = false}); COUNT is a Long, the column an Integer, hence the cast.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookChapter c SET c.versionsCount = CAST((SELECT COUNT(v) FROM BookChapterVersion v WHERE v.chapter.id = :chapterId) AS Integer)"
            + " WHERE c.id = :chapterId")
    void refreshVersionsCount(@Param("chapterId") UUID chapterId);

    @Query("SELECT c.book.id FROM BookChapter c WHERE c.id = :chapterId")
    Optional<UUID> findBookIdByChapterId(@Param("chapterId") UUID chapterId);
}
