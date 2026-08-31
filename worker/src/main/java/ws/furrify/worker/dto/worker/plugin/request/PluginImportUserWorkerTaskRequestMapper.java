package ws.furrify.worker.dto.worker.plugin.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface PluginImportUserWorkerTaskRequestMapper extends BaseRequestMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, CreatePluginImportUserWorkerTaskRequest> {
    @Override
    PluginImportUserWorkerTaskDTO toDto(CreatePluginImportUserWorkerTaskRequest createPluginImportUserWorkerTaskRequest);
}