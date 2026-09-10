package ws.furrify.worker.dto.worker.plugin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.CreateUserWorkerTaskRequest;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreatePluginImportUserWorkerTaskRequest extends CreateUserWorkerTaskRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {

    @NotNull
    private UUID fileReferenceId;
    @NotNull
    private UUID destinationLibraryReferenceId;

    @NotBlank
    private String provider;
}
