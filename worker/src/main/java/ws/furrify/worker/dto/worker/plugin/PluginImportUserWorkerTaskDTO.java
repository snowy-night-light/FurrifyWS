package ws.furrify.worker.dto.worker.plugin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;
import ws.furrify.worker.shared.plugin.WorkerPluginIntf;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class PluginImportUserWorkerTaskDTO extends UserWorkerTaskDTO<PluginImportUserWorkerTask> {
    private WorkerPluginIntf workerPlugin;
}
