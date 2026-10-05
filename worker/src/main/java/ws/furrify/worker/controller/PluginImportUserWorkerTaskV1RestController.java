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
package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.ImportWorkerPluginDTO;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.CreatePluginImportUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.plugin.request.PatchPluginImportUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.plugin.PluginImportUserWorkerTaskEntityService;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@RestController
@RequestMapping("/v1/workers/user/plugin/import")
public class PluginImportUserWorkerTaskV1RestController extends UserWorkerTaskBaseRestController<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest, PatchPluginImportUserWorkerTaskRequest> {

    private final PluginImportUserWorkerTaskEntityService pluginImportUserWorkerTaskEntityService;

    @Autowired
    public PluginImportUserWorkerTaskV1RestController(BaseRequestMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest> requestDtoMapper, PluginImportUserWorkerTaskEntityService entityCrudService) {
        super(requestDtoMapper, entityCrudService);
        this.pluginImportUserWorkerTaskEntityService = entityCrudService;
    }

    @GetMapping(value = "/list", produces = {APPLICATION_JSON})
    @ResponseStatus(value = HttpStatus.OK)
    protected List<ImportWorkerPluginDTO> getInstalledPlugins() {
        return pluginImportUserWorkerTaskEntityService.getAllPlugins();
    }
}
