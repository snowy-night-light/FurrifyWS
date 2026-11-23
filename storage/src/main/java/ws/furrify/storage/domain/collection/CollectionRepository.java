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
package ws.furrify.storage.domain.collection;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CollectionRepository extends BaseEntityRepository<Collection> {

    /** The library's collections; Hibernate removes their post links first. */
    @Modifying
    @Query("DELETE FROM Collection c WHERE c.library.id = :library")
    int deleteByLibrary(@Param("library") UUID library);

    /** Collections holding any of these posts (to unlink them before the posts go). */
    @Query("SELECT DISTINCT c FROM Collection c JOIN c.posts p WHERE p.id IN :posts")
    List<Collection> findHoldingPosts(@Param("posts") List<UUID> posts);
}
