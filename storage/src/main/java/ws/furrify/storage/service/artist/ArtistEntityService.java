package ws.furrify.storage.service.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;

import java.util.UUID;

@Service
public class ArtistEntityService extends BaseEntityCrudService<Artist, ArtistDTO, PatchArtistRequest> {

    private final SourceEntityService sourceEntityService;
    private final LibraryEntityService libraryEntityService;
    private final MediaEntityService mediaEntityService;

    @Autowired
    public ArtistEntityService(BaseEntityRepository<Artist> entityRepository, BaseDTOMapper<Artist, ArtistDTO, PatchArtistRequest> dtoMapper, SourceEntityService sourceEntityService, LibraryEntityService libraryEntityService, MediaEntityService mediaEntityService) {
        super(entityRepository, dtoMapper);
        this.sourceEntityService = sourceEntityService;
        this.libraryEntityService = libraryEntityService;
        this.mediaEntityService = mediaEntityService;
    }

    @Override
    public ArtistDTO create(ArtistDTO dto) {
        super.handleCreateInternalReference(dto, ArtistDTO::getAvatar, ArtistDTO::setAvatar, mediaEntityService);
        super.handleCreateInternalReference(dto, ArtistDTO::getLibrary, ArtistDTO::setLibrary, libraryEntityService);
        super.handleCreateInternalCollectionReferences(dto, ArtistDTO::getSources, ArtistDTO::setSources, sourceEntityService);

        return super.create(dto);
    }

    @Override
    public ArtistDTO patchById(UUID id, PatchArtistRequest patchDto) {
        super.handlePatchInternalReference(patchDto.getAvatar(), mediaEntityService);
        super.handlePatchInternalReference(patchDto.getLibrary(), libraryEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getSources(), mediaEntityService);

        return super.patchById(id, patchDto);
    }
}
