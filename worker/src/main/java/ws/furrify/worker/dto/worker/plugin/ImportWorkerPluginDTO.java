package ws.furrify.worker.dto.worker.plugin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ImportWorkerPluginDTO {
    private String provider;
    private String name;
    private String[] allowedExtensions;
}
