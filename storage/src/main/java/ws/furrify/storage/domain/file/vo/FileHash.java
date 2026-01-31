package ws.furrify.storage.domain.file.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * File hash wrapper.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class FileHash {

    @NonNull
    @NotBlank
    private String hash;

    @NonNull
    private FileHashType type;
}