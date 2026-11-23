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
package ws.furrify.storage.domain.artist;

import org.springframework.stereotype.Repository;
import ws.furrify.core.entity.BaseEntityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArtistRepository extends BaseEntityRepository<Artist> {

    @Query("SELECT a.id FROM Artist a WHERE a.library.id = :library")
    List<UUID> findIdsByLibrary(@Param("library") UUID library, Limit limit);

    @Query("SELECT s.id FROM Artist a JOIN a.sources s WHERE a.id IN :artists")
    List<UUID> findSourceIdsOfArtists(@Param("artists") List<UUID> artists);

    /** The artists; Hibernate removes their nicknames and source links first. */
    @Modifying
    @Query("DELETE FROM Artist a WHERE a.id IN :artists")
    int deleteByIds(@Param("artists") List<UUID> artists);
}
