package ws.furrify.storage.controllers.stats;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ws.furrify.storage.dto.stats.UserStatisticsDto;
import ws.furrify.storage.service.stats.UserStatisticsService;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/{userId}/statistics")
public class UserStatisticsV1RestController {

    private final UserStatisticsService userStatisticsService;

    @GetMapping(produces = {APPLICATION_JSON})
    @ResponseStatus(value = HttpStatus.OK)
    protected UserStatisticsDto getUserStatistics(@PathVariable String userId) {
        return userStatisticsService.getUserStatistics(userId);
    }

}
