package ws.furrify.attachment.controller.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.AttachmentFileRequestMapper;
import ws.furrify.attachment.dto.file.request.CreateAttachmentFileRequest;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.service.file.AttachmentFileEntityService;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;

import java.util.UUID;

@RestController
@RequestMapping("/v1/storage/files")
class AttachmentFileV1RestController extends BaseEntityRestController<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest, PatchAttachmentFileRequest> {

    private final AttachmentFileEntityService attachmentFileEntityService;
    private final AttachmentFileRequestMapper attachmentFileRequestDtoMapper;

    @Autowired
    public AttachmentFileV1RestController(BaseRequestMapper<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest> requestDtoMapper, BaseEntityCrudService<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
        this.attachmentFileEntityService = (AttachmentFileEntityService) entityCrudService;
        this.attachmentFileRequestDtoMapper = (AttachmentFileRequestMapper) requestDtoMapper;
    }

    @Override
    protected AttachmentFileDTO save(@ModelAttribute CreateAttachmentFileRequest dto) {
        return attachmentFileEntityService.createWithFileUpload(attachmentFileRequestDtoMapper.toDto(dto), dto.getFile());
    }

    @Override
    protected AttachmentFileDTO patch(UUID id, @ModelAttribute PatchAttachmentFileRequest patchRequestDto) {
        return attachmentFileEntityService.patchWithFileUpload(id, patchRequestDto, patchRequestDto.getFile().orElse(null));
    }
}
