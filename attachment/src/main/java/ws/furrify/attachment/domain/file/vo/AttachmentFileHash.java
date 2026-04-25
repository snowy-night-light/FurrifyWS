package ws.furrify.attachment.domain.file.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * AttachmentFile hash wrapper.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AttachmentFileHash {

    @NonNull
    @NotBlank
    private String hash;

    @NonNull
    private AttachmentFileHashType type;
}