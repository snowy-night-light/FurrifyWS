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
package ws.furrify.storage.domain.tag;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TagRepository extends BaseEntityRepository<Tag> {

    /** The library's tags, and tags elsewhere using one of its categories (a tag can't be without its category). */
    @Query("SELECT t.id FROM Tag t WHERE t.library.id = :library OR t.category.library.id = :library")
    List<UUID> findIdsByLibrary(@Param("library") UUID library, Limit limit);

    @Modifying
    @Query("DELETE FROM Tag t WHERE t.id IN :tags")
    int deleteByIds(@Param("tags") List<UUID> tags);
}
