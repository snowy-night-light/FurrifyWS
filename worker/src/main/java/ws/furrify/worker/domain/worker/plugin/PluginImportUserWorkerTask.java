package ws.furrify.worker.domain.worker.plugin;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.converters.PluginDBConverter;
import ws.furrify.worker.domain.worker.UserWorkerTask;
import ws.furrify.worker.shared.plugin.WorkerPluginIntf;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginImportUserWorkerTask extends UserWorkerTask {
    @Convert(converter = PluginDBConverter.class)
    WorkerPluginIntf workerPlugin;
}
