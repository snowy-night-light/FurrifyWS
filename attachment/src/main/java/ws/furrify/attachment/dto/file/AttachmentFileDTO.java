package ws.furrify.attachment.dto.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.MimeType;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.dto.file.vo.AttachmentFileHashDTO;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;

import java.net.URI;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AttachmentFileDTO extends UserScopedEntityDTO<AttachmentFile> {
    private String fileName;

    private List<AttachmentFileHashDTO> fileHashes;

    private MimeType mimeType;

    private FileUploadStatus uploadStatus;

    private Long fileSize;

    private URI fileUri;
    private URI thumbnailUri;

    private String storageServiceId;
}
