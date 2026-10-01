package ws.furrify.worker.dto.worker.plugin;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.worker.dto.worker.plugin.request.PatchPluginImportUserWorkerTaskRequest;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface PluginImportUserWorkerTaskDTOMapper extends BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, PatchPluginImportUserWorkerTaskRequest> {
}