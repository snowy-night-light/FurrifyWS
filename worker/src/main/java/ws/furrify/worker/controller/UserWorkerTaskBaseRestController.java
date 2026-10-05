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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.worker.domain.worker.UserWorkerTask;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.CreateUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.request.PatchUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;

import java.util.UUID;

public abstract class UserWorkerTaskBaseRestController<ENTITY extends UserWorkerTask, DTO extends UserWorkerTaskDTO<ENTITY>, CREATE_REQ extends CreateUserWorkerTaskRequest<ENTITY, DTO>, PATCH_REQ extends PatchUserWorkerTaskRequest<ENTITY, DTO>> extends BaseEntityRestController<ENTITY, DTO, CREATE_REQ, PATCH_REQ> {
    private final UserWorkerTaskBaseEntityService<ENTITY, DTO, PATCH_REQ> entityCrudService;

    public UserWorkerTaskBaseRestController(BaseRequestMapper<ENTITY, DTO, CREATE_REQ> requestDtoMapper, UserWorkerTaskBaseEntityService<ENTITY, DTO, PATCH_REQ> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
        this.entityCrudService = entityCrudService;
    }

    @PostMapping("/{id}/execute")
    @ResponseStatus(HttpStatus.OK)
    public void triggerExecution(@PathVariable UUID id) {
        entityCrudService.triggerExecution(id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancel(@PathVariable UUID id) {
        entityCrudService.cancelById(id);
    }
}
