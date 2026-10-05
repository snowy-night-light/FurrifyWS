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
