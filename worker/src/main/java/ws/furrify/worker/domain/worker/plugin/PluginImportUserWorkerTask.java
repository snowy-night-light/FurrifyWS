package ws.furrify.worker.domain.worker.plugin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.UserWorkerTask;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginImportUserWorkerTask extends UserWorkerTask {
    @Column(nullable = false)
    @NotNull
    UUID fileReferenceId;

    @Column(nullable = false)
    @NotBlank
    String provider;

    @Column(nullable = false)
    UUID destinationLibraryReferenceId;
}
