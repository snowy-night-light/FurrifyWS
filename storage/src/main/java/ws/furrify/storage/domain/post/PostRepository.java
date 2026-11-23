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
package ws.furrify.storage.domain.post;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PostRepository extends BaseEntityRepository<Post> {

    @Query("SELECT p.id FROM Post p WHERE p.library.id = :library")
    List<UUID> findIdsByLibrary(@Param("library") UUID library, Limit limit);

    @Query("SELECT s.id FROM Post p JOIN p.sources s WHERE p.id IN :posts")
    List<UUID> findSourceIdsOfPosts(@Param("posts") List<UUID> posts);

    /** The posts; Hibernate removes their rows in their own join tables (tags, artists, media, sources) first. */
    @Modifying
    @Query("DELETE FROM Post p WHERE p.id IN :posts")
    int deleteByIds(@Param("posts") List<UUID> posts);

    /** Posts linked to any of these tags (to unlink them before the tags go). */
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id IN :tags")
    List<Post> findLinkedToTags(@Param("tags") List<UUID> tags);

    /** Posts linked to any of these artists (to unlink them before the artists go). */
    @Query("SELECT DISTINCT p FROM Post p JOIN p.artists a WHERE a.id IN :artists")
    List<Post> findLinkedToArtists(@Param("artists") List<UUID> artists);
}
