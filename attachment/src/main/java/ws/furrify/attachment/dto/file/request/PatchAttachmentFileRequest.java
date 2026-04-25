package ws.furrify.attachment.dto.file.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.core.entity.request.BasePatchEntityRequest;

@Data
public class PatchAttachmentFileRequest implements BasePatchEntityRequest<AttachmentFile, AttachmentFileDTO> {

    private JsonNullable<@NotBlank String> fileName = JsonNullable.undefined();

    private JsonNullable<@NotNull MultipartFile> file = JsonNullable.undefined();
}
