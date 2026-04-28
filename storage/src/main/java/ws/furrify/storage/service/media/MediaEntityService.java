package ws.furrify.storage.service.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.media.request.PatchMediaRequest;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.shared.exception.StorageErrors;

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
    public MediaDTO create(MediaDTO dto) {
        if (attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(dto.getFileReferenceId()).getBody() == null) {
            throw new ReferenceNotFoundException(StorageErrors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
        }

        dto.setSources(
                dto.getSources().stream()
                        .map(source -> this.sourceEntityService.findById(source.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(source.getId()))))
                        .toList()
        );

        return super.create(dto);
    }

    @Override
    public MediaDTO patchById(UUID id, PatchMediaRequest patchDto) {
        if (patchDto.getFileReferenceId().isPresent() && attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(patchDto.getFileReferenceId().get()) == null) {
            throw new ServiceLogicException(StorageErrors.REFERENCE_NOT_FOUND.getErrorMessage(patchDto.getFileReferenceId()));
        }

        if (patchDto.getSources().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getSources().get()) {
                if (!this.sourceEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }

        return super.patchById(id, patchDto);
    }
}
