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
package ws.furrify.storage.dto.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UserStatisticsDto {

    private long postsCount;
    private long booksCount;
    private long collectionsCount;
    private long librariesCount;
    private long tagsCount;
    private long artistsCount;

    private long imagesCount;
    private long videoCount;
    private long animationCount;
    private long musicCount;

    private String ownerId;

    private List<DailyUserStatisticsChartData> last7DaysChart;

    @Data
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    public static class DailyUserStatisticsChartData {
        private ZonedDateTime date;
        private long newBooksCount;
        private long newPostsCount;
        private long newCollectionsCount;
        private long newTagsCount;
        private long newArtistsCount;
    }
}
