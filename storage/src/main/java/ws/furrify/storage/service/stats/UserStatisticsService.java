package ws.furrify.storage.service.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.ArtistRepository;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.collection.CollectionRepository;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.library.LibraryRepository;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.post.PostRepository;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.dto.stats.UserStatisticsDto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specGreaterThanOrEqual;
import static ws.furrify.core.specification.EntitySpec.specLessThanOrEqual;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {

    private final PostRepository postRepository;
    private final CollectionRepository collectionRepository;
    private final LibraryRepository libraryRepository;
    private final TagRepository tagRepository;
    private final ArtistRepository artistRepository;
    private final ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient attachmentClient;

    
    private long getAttachmentCountByLike(String mimeTypePattern) {
        String spec = EntitySpec.from(SecurityContextUtils.getUserScopedSecuritySpec())
                .and().where("mimeType", EntitySpec.specLike(mimeTypePattern))
                .build().specString();

        org.openapitools.model.Pageable pageable = new org.openapitools.model.Pageable();
        pageable.setSize(1);

        var response = attachmentClient.getAllPaged(pageable, spec);
        if (response.getBody() != null && response.getBody().getPage() != null && response.getBody().getPage().getTotalElements() != null) {
            return response.getBody().getPage().getTotalElements();
        }
        return 0L;
    }

    public UserStatisticsDto getUserStatistics(String userId) {
        String currentUserId = SecurityContextUtils.getCurrentSubject()
                .map(UUID::toString)
                .orElseThrow(() -> new IllegalStateException("Current user subject was not found."));

        if (!currentUserId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view these statistics.");
        }

        // Get total counts
        long postsCount = postRepository.count(SecurityContextUtils.<Post>getUserScopedSecuritySpec().specification());
        long collectionsCount = collectionRepository.count(SecurityContextUtils.<Collection>getUserScopedSecuritySpec().specification());
        long librariesCount = libraryRepository.count(SecurityContextUtils.<Library>getUserScopedSecuritySpec().specification());
        long tagsCount = tagRepository.count(SecurityContextUtils.<Tag>getUserScopedSecuritySpec().specification());
        long artistsCount = artistRepository.count(SecurityContextUtils.<Artist>getUserScopedSecuritySpec().specification());

        // 7 days chart
        List<UserStatisticsDto.DailyUserStatisticsChartData> last7DaysChart = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            ZonedDateTime startOfDay = date.atStartOfDay(ZoneOffset.UTC);
            ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1);

            long newPosts = postRepository.count(
                    EntitySpec.from(SecurityContextUtils.<Post>getUserScopedSecuritySpec())
                            .and().where("createdAt", specGreaterThanOrEqual(startOfDay))
                            .and().where("createdAt", specLessThanOrEqual(endOfDay))
                            .build()
                            .specification()
            );

            long newCollections = collectionRepository.count(
                    EntitySpec.from(SecurityContextUtils.<Collection>getUserScopedSecuritySpec())
                            .and().where("createdAt", specGreaterThanOrEqual(startOfDay))
                            .and().where("createdAt", specLessThanOrEqual(endOfDay))
                            .build()
                            .specification()
            );

            long newTags = tagRepository.count(
                    EntitySpec.from(SecurityContextUtils.<Tag>getUserScopedSecuritySpec())
                            .and().where("createdAt", specGreaterThanOrEqual(startOfDay))
                            .and().where("createdAt", specLessThanOrEqual(endOfDay))
                            .build()
                            .specification()
            );

            long newArtists = artistRepository.count(
                    EntitySpec.from(SecurityContextUtils.<Artist>getUserScopedSecuritySpec())
                            .and().where("createdAt", specGreaterThanOrEqual(startOfDay))
                            .and().where("createdAt", specLessThanOrEqual(endOfDay))
                            .build()
                            .specification()
            );

            last7DaysChart.add(UserStatisticsDto.DailyUserStatisticsChartData.builder()
                    .date(startOfDay)
                    .newPostsCount(newPosts)
                    .newCollectionsCount(newCollections)
                    .newTagsCount(newTags)
                    .newArtistsCount(newArtists)
                    .build());
        }

        
        long gifAnimationCount = getAttachmentCountByLike("image/gif");
        long flashAnimationCount = getAttachmentCountByLike("application/x-shockwave-flash");
        long animationCount = gifAnimationCount + flashAnimationCount;
        long imagesCount = getAttachmentCountByLike("image/%") - gifAnimationCount;
        long videoCount = getAttachmentCountByLike("video/%");
        long musicCount = getAttachmentCountByLike("audio/%");

        return UserStatisticsDto.builder()
                .ownerId(userId)
                .postsCount(postsCount)
                .collectionsCount(collectionsCount)
                .librariesCount(librariesCount)
                .tagsCount(tagsCount)
                                .artistsCount(artistsCount)
                .imagesCount(imagesCount)
                .videoCount(videoCount)
                .animationCount(animationCount)
                .musicCount(musicCount)
                .last7DaysChart(last7DaysChart)
                .build();
    }
}
