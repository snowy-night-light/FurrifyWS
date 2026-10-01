package ws.furrify.worker.dto.worker.plugin.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.PatchUserWorkerTaskRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatchPluginImportUserWorkerTaskRequest extends PatchUserWorkerTaskRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {
}
