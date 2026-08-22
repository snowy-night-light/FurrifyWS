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
        private long newPostsCount;
        private long newCollectionsCount;
        private long newTagsCount;
        private long newArtistsCount;
    }
}
