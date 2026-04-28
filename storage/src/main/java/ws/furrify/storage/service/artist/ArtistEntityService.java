package ws.furrify.storage.service.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;

import java.util.UUID;

@Service
public class ArtistEntityService extends BaseEntityCrudService<Artist, ArtistDTO, PatchArtistRequest> {

    private final SourceEntityService sourceEntityService;
    private final MediaEntityService mediaEntityService;

    @Autowired
    public ArtistEntityService(BaseEntityRepository<Artist> entityRepository, BaseDTOMapper<Artist, ArtistDTO, PatchArtistRequest> dtoMapper, SourceEntityService sourceEntityService, MediaEntityService mediaEntityService) {
        super(entityRepository, dtoMapper);
        this.sourceEntityService = sourceEntityService;
        this.mediaEntityService = mediaEntityService;
    }

    @Override
    public ArtistDTO create(ArtistDTO dto) {
        if (dto.getAvatar() != null) {
            dto.setAvatar(
                    this.mediaEntityService.findById(dto.getAvatar().getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getAvatar().getId())))
            );
        }
        dto.setSources(
                dto.getSources().stream()
                        .map(source -> this.sourceEntityService.findById(source.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getAvatar().getId()))))
                        .toList()
        );

        return super.create(dto);
    }

    @Override
    public ArtistDTO patchById(UUID id, PatchArtistRequest patchDto) {
        if (patchDto.getAvatar().isPresent() && !this.mediaEntityService.existsById(patchDto.getAvatar().get().getId())) {
            throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(patchDto.getAvatar().get().getId()));
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
