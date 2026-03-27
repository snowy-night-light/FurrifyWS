package ws.furrify.storage.service.artist;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.service.media.MediaEntityCrudService;
import ws.furrify.storage.service.source.SourceEntityCrudService;

import java.util.UUID;

@Service
public class ArtistEntityCrudService extends BaseEntityCrudService<Artist, ArtistDTO, PatchArtistRequest> {

    private final SourceEntityCrudService sourceEntityCrudService;
    private final MediaEntityCrudService mediaEntityCrudService;

    @Autowired
    public ArtistEntityCrudService(BaseEntityRepository<Artist> entityRepository, BaseDTOMapper<Artist, ArtistDTO, PatchArtistRequest> dtoMapper, SourceEntityCrudService sourceEntityCrudService, MediaEntityCrudService mediaEntityCrudService) {
        super(entityRepository, dtoMapper);
        this.sourceEntityCrudService = sourceEntityCrudService;
        this.mediaEntityCrudService = mediaEntityCrudService;
    }

    @Override
    public ArtistDTO create(ArtistDTO dto) {
        if (dto.getAvatar() != null) {
            dto.setAvatar(
                    this.mediaEntityCrudService.findById(dto.getAvatar().getId()).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getAvatar().getId())))
            );
        }
        dto.setSources(
                dto.getSources().stream()
                        .map(source -> this.sourceEntityCrudService.findById(source.getId()).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getAvatar().getId()))))
                        .toList()
        );

        return super.create(dto);
    }

    @Override
    public ArtistDTO patchById(UUID id, PatchArtistRequest patchDto) {
        if (patchDto.getAvatar().isPresent() && !this.mediaEntityCrudService.existsById(patchDto.getAvatar().get().getId())) {
            throw new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(patchDto.getAvatar().get().getId()));
        }

        if (patchDto.getSources().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getSources().get()) {
                if (!this.sourceEntityCrudService.existsById(entityIdRequest.getId())) {
                    throw new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(patchDto.getAvatar().get().getId()));
                }
            }

        }

        return super.patchById(id, patchDto);
    }
}
