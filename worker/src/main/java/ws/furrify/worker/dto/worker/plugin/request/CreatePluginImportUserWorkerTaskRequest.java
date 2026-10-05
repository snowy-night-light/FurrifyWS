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
package ws.furrify.worker.dto.worker.plugin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.CreateUserWorkerTaskRequest;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreatePluginImportUserWorkerTaskRequest extends CreateUserWorkerTaskRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {

    @NotNull
    private UUID fileReferenceId;
    @NotNull
    private UUID destinationLibraryReferenceId;

    @NotNull
    private Boolean downloadExternalMedia;

    @NotBlank
    private String provider;
}
