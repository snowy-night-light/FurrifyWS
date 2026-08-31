package ws.furrify.worker.dto.worker.plugin;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface PluginImportUserWorkerTaskDTOMapper extends BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {
}