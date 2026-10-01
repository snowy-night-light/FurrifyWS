package ws.furrify.attachment.dto.file.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface AttachmentFileRequestMapper extends BaseRequestMapper<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest> {
    @Override
    AttachmentFileDTO toDto(CreateAttachmentFileRequest createAttachmentFileRequest);
}
