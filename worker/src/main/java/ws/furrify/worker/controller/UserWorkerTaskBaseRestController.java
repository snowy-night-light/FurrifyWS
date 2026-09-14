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

abstract class UserWorkerTaskBaseRestController<ENTITY extends UserWorkerTask, DTO extends UserWorkerTaskDTO<ENTITY>, CREATE_REQ extends CreateUserWorkerTaskRequest<ENTITY, DTO>, PATCH_REQ extends PatchUserWorkerTaskRequest<ENTITY, DTO>> extends BaseEntityRestController<ENTITY, DTO, CREATE_REQ, PATCH_REQ> {
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
}
