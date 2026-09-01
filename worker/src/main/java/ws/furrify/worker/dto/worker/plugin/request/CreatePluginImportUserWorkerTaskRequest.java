package ws.furrify.worker.dto.worker.plugin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class CreatePluginImportUserWorkerTaskRequest implements BaseCreateEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO> {

    @NotNull
    private UUID fileReferenceId;
    @NotNull
    private UUID destinationLibraryReferenceId;

    @NotBlank
    private String provider;

    @NotNull
    private ZonedDateTime startAt;
}
