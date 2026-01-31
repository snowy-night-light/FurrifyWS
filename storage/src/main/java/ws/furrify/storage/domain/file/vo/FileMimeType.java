package ws.furrify.storage.domain.file.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * File mimetype wrapper.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class FileMimeType {

    @NonNull
    @NotBlank
    @Pattern(regexp = "\\w+/[-+.\\w]+")
    private String mimeType;

}