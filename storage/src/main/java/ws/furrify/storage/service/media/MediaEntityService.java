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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaEntityService extends BaseEntityCrudService<Media, MediaDTO, PatchMediaRequest> {

    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final SourceEntityService sourceEntityService;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public MediaEntityService(BaseEntityRepository<Media> entityRepository, BaseDTOMapper<Media, MediaDTO, PatchMediaRequest> dtoMapper, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, SourceEntityService sourceEntityService, PlatformTransactionManager transactionManager) {
        super(entityRepository, dtoMapper);
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.sourceEntityService = sourceEntityService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /*
     * The attachment service is asked whether the file exists BEFORE the transaction starts: a
     * remote call inside it holds a database connection for as long as the attachment service
     * takes to answer, and under load (an import uploading files) that drains the pool for
     * every other request. The writes themselves then run in a short transaction.
     */

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MediaDTO create(MediaDTO dto) {
        requireAttachment(dto.getFileReferenceId());
        return transactionTemplate.execute(status -> super.create(dto));
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<MediaDTO> createBulk(List<MediaDTO> dtos) {
        dtos.stream().map(MediaDTO::getFileReferenceId).distinct().forEach(this::requireAttachment);
        return transactionTemplate.execute(status -> super.createBulk(dtos));
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MediaDTO patchById(UUID id, PatchMediaRequest patchDto) {
        if (patchDto.getFileReferenceId().isPresent()) {
            requirePatchedAttachment(patchDto.getFileReferenceId().get());
        }
        return transactionTemplate.execute(status -> super.patchById(id, patchDto));
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<MediaDTO> patchBulk(Map<UUID, PatchMediaRequest> patchDtos) {
        if (patchDtos != null) {
            patchDtos.values().stream()
                    .filter(p -> p.getFileReferenceId().isPresent())
                    .map(p -> p.getFileReferenceId().get())
                    .distinct()
                    .forEach(this::requirePatchedAttachment);
        }
        return transactionTemplate.execute(status -> super.patchBulk(patchDtos));
    }

    private void requireAttachment(UUID fileReferenceId) {
        if (fileReferenceId == null || attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileReferenceId).getBody() == null) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(fileReferenceId));
        }
    }

    private void requirePatchedAttachment(UUID fileReferenceId) {
        if (fileReferenceId == null || attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(fileReferenceId).getBody() == null) {
            throw new ServiceLogicException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(fileReferenceId));
        }
    }

    @Override
    protected MediaDTO handleCreate(MediaDTO dto) {
        // attachment checked in create/createBulk, outside the transaction
        super.handleInternalCollectionReferences(dto, MediaDTO::getSources, MediaDTO::setSources, sourceEntityService);

        return super.handleCreate(dto);
    }

    @Override
    protected MediaDTO handlePatch(UUID id, PatchMediaRequest patchDto) {
        // attachment checked in patchById/patchBulk, outside the transaction
        super.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        return super.handlePatch(id, patchDto);
    }

}
