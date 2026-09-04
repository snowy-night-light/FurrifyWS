package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.CreatePluginImportUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.plugin.PluginImportUserWorkerTaskEntityService;

import java.util.UUID;


@RestController
@RequestMapping("/v1/user/workers/plugin/import")
class PluginImportUserWorkerTaskV1RestController extends BaseEntityRestController<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {

    private final PluginImportUserWorkerTaskEntityService pluginImportUserWorkerTaskEntityService;

    @Autowired
    public PluginImportUserWorkerTaskV1RestController(BaseRequestMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest> requestDtoMapper, PluginImportUserWorkerTaskEntityService entityCrudService) {
        pluginImportUserWorkerTaskEntityService = entityCrudService;

        super(requestDtoMapper, entityCrudService);
    }

    @PostMapping("/{id}/execute")
    @ResponseStatus(HttpStatus.OK)
    protected void triggerExecution(@PathVariable UUID id) {
        pluginImportUserWorkerTaskEntityService.triggerExecution(id);
    }
}
