package ws.furrify.worker.dto.worker.plugin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;

import java.time.ZonedDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class PluginImportUserWorkerTaskDTO extends UserWorkerTaskDTO<PluginImportUserWorkerTask> {
    private String provider;
    private UUID fileReferenceId;
    private UUID destinationLibraryReferenceId;
    private ZonedDateTime startAt;
}
