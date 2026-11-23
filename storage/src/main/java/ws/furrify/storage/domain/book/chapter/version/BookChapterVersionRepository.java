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
package ws.furrify.storage.domain.book.chapter.version;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookChapterVersionRepository extends BaseEntityRepository<BookChapterVersion> {

    @Query("SELECT COALESCE(MAX(v.chapterVersion), 0) FROM BookChapterVersion v WHERE v.chapter.id = :chapterId")
    int findHighestChapterVersionByChapterId(@Param("chapterId") UUID chapterId);

    /**
     * Word counts of the chapter's versions, newest content first (callers take the first). Ordered
     * by content date, not version number: a historic version imported later gets the highest
     * number but holds older content.
     */
    @Query("SELECT v.wordCount FROM BookChapterVersion v WHERE v.chapter.id = :chapterId ORDER BY v.contentUpdatedAt DESC, v.chapterVersion DESC")
    List<Long> findLatestWordCountByChapterId(@Param("chapterId") UUID chapterId);

    @Query("SELECT v.wordCount FROM BookChapterVersion v WHERE v.id = :versionId")
    Optional<Long> findWordCountById(@Param("versionId") UUID versionId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookChapterVersion v SET v.wordCount = :wordCount WHERE v.id = :versionId")
    int updateWordCount(@Param("versionId") UUID versionId, @Param("wordCount") long wordCount);

}
