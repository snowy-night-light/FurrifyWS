package ws.furrify.worker.service.worker.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;

@Service
public class PluginImportUserWorkerTaskEntityService extends BaseEntityCrudService<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {

    @Autowired
    public PluginImportUserWorkerTaskEntityService(BaseEntityRepository<PluginImportUserWorkerTask> entityRepository, BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
