package ws.furrify.attachment.controller.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/v1/files")
class AttachmentFileV1RestController extends BaseEntityRestController<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest, PatchAttachmentFileRequest> {

    private final AttachmentFileEntityService attachmentFileEntityService;
    private final AttachmentFileRequestMapper attachmentFileRequestDtoMapper;

    @Autowired
    public AttachmentFileV1RestController(BaseRequestMapper<AttachmentFile, AttachmentFileDTO, CreateAttachmentFileRequest> requestDtoMapper, BaseEntityCrudService<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
        this.attachmentFileEntityService = (AttachmentFileEntityService) entityCrudService;
        this.attachmentFileRequestDtoMapper = (AttachmentFileRequestMapper) requestDtoMapper;
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = CreateAttachmentFileRequest.class)
                    )
            )
    )
    @PostMapping(consumes = "multipart/form-data", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.CREATED)
    protected AttachmentFileDTO saveWithUpload(@ModelAttribute CreateAttachmentFileRequest dto) {
        return attachmentFileEntityService.createWithFileUpload(attachmentFileRequestDtoMapper.toDto(dto), dto.getFile());
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = PatchAttachmentFileRequest.class)
                    )
            )
    )
    @PatchMapping(value = "/{id}", consumes = "multipart/form-data", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected AttachmentFileDTO patchWithUpload(@PathVariable UUID id, @ModelAttribute PatchAttachmentFileRequest patchRequestDto) {
        return attachmentFileEntityService.patchWithFileUpload(id, patchRequestDto, patchRequestDto.getFile().orElse(null));
    }
}
