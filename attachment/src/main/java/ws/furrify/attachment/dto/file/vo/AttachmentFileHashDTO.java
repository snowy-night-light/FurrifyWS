package ws.furrify.attachment.dto.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;

@EqualsAndHashCode
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentFileHashDTO {
    private AttachmentFileHashType type;

    private String hash;
}
