package ws.furrify.attachment.dto.file;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface AttachmentFileDTOMapper extends BaseDTOMapper<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> {
}