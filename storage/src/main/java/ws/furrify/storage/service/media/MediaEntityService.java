/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.service.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.media.request.PatchMediaRequest;
import ws.furrify.storage.service.source.SourceEntityService;

import java.util.UUID;

@Service
public class MediaEntityService extends BaseEntityCrudService<Media, MediaDTO, PatchMediaRequest> {

    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final SourceEntityService sourceEntityService;

    @Autowired
    public MediaEntityService(BaseEntityRepository<Media> entityRepository, BaseDTOMapper<Media, MediaDTO, PatchMediaRequest> dtoMapper, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, SourceEntityService sourceEntityService) {
        super(entityRepository, dtoMapper);
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.sourceEntityService = sourceEntityService;
    }

    @Override
    protected MediaDTO handleCreate(MediaDTO dto) {
        if (attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(dto.getFileReferenceId()).getBody() == null) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
        }

        super.handleInternalCollectionReferences(dto, MediaDTO::getSources, MediaDTO::setSources, sourceEntityService);

        return super.handleCreate(dto);
    }

    @Override
    protected MediaDTO handlePatch(UUID id, PatchMediaRequest patchDto) {
        if (patchDto.getFileReferenceId().isPresent() && attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(patchDto.getFileReferenceId().get()).getBody() == null) {
            throw new ServiceLogicException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(patchDto.getFileReferenceId()));
        }

        super.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        return super.handlePatch(id, patchDto);
    }

}
