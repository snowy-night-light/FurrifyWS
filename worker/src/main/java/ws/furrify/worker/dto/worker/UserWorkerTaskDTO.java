/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.dto.worker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.worker.domain.worker.WorkStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UserWorkerTaskDTO<ENTITY extends UserScopedEntity> extends UserScopedEntityDTO<ENTITY> {
    private List<String> errors;
    private List<String> warnings;

    private String log;

    private WorkStatus status = WorkStatus.NOT_STARTED;

    private ZonedDateTime startAt;
    private ZonedDateTime startedAt;
    private ZonedDateTime finishedAt;

    private UUID launchId;
}
