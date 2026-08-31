package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.CreatePluginImportUserWorkerTaskRequest;


@RestController
@RequestMapping("/v1/user/workers/plugin/import")
class PluginImportUserWorkerTaskV1RestController extends BaseEntityRestController<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {

    @Autowired
    public PluginImportUserWorkerTaskV1RestController(BaseRequestMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest> requestDtoMapper, BaseEntityCrudService<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
