package ws.furrify.attachment.dto.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;

@Data
public class CreateAttachmentFileRequest implements BaseCreateEntityRequest<AttachmentFile, AttachmentFileDTO> {

    @NotBlank
    private String fileName;

    @NotNull
    private MultipartFile file;
}
