package ws.furrify.worker.dto.worker.plugin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.shared.plugin.WorkerPluginIntf;

@Data
public class CreatePluginImportUserWorkerTaskRequest implements BaseCreateEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {

    @NotNull
    private WorkerPluginIntf workerPlugin;
}
