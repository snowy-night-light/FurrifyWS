/*
 * furrify-attachment-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.attachment.controller.file;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.AttachmentFileRequestMapper;
import ws.furrify.attachment.dto.file.request.CreateAttachmentFileRequest;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.attachment.service.file.AttachmentFileEntityService;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;

import java.util.List;
import java.util.Map;
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

    @Override
    @PostMapping(produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    protected AttachmentFileDTO save(CreateAttachmentFileRequest dto) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Use multipart/form-data endpoint instead.");
    }

    @Override
    @PatchMapping(value = "/{id}", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    protected AttachmentFileDTO patch(UUID id, PatchAttachmentFileRequest patchRequestDto) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Use multipart/form-data endpoint instead.");
    }

    @Override
    @PostMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Hidden
    protected List<AttachmentFileDTO> saveBulk(List<CreateAttachmentFileRequest> dtos) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, AttachmentErrors.BULK_CREATION_NOT_ALLOWED.getErrorMessage());
    }

    @Override
    @PatchMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Hidden
    protected List<AttachmentFileDTO> patchBulk(Map<UUID, PatchAttachmentFileRequest> patchRequestDtos) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, AttachmentErrors.BULK_CREATION_NOT_ALLOWED.getErrorMessage());
    }
}
