package ws.furrify.storage.service.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.request.PatchPostRequest;
import ws.furrify.storage.service.artist.ArtistEntityService;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.service.tag.TagEntityService;

import java.util.UUID;

@Service
public class PostEntityService extends BaseEntityCrudService<Post, PostDTO, PatchPostRequest> {

    private final TagEntityService tagEntityService;
    private final ArtistEntityService artistEntityService;
    private final SourceEntityService sourceEntityService;
    private final MediaEntityService mediaEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public PostEntityService(BaseEntityRepository<Post> entityRepository, BaseDTOMapper<Post, PostDTO, PatchPostRequest> dtoMapper, TagEntityService tagEntityService, ArtistEntityService artistEntityService, SourceEntityService sourceEntityService, MediaEntityService mediaEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.sourceEntityService = sourceEntityService;
        this.mediaEntityService = mediaEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    public PostDTO patchById(UUID id, PatchPostRequest patchDto) {
        super.handlePatchCollectionInternalReferences(patchDto.getTags(), tagEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getArtists(), artistEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getDisplayMediaList(), mediaEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getAttachments(), mediaEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getSources(), sourceEntityService);
        super.handlePatchInternalReference(patchDto.getLibrary(), libraryEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public PostDTO create(PostDTO dto) {
        super.handleCreateInternalCollectionReferences(dto, PostDTO::getTags, PostDTO::setTags, tagEntityService);
        super.handleCreateInternalCollectionReferences(dto, PostDTO::getArtists, PostDTO::setArtists, artistEntityService);
        super.handleCreateInternalCollectionReferences(dto, PostDTO::getDisplayMediaList, PostDTO::setDisplayMediaList, mediaEntityService);
        super.handleCreateInternalCollectionReferences(dto, PostDTO::getAttachments, PostDTO::setAttachments, mediaEntityService);
        super.handleCreateInternalCollectionReferences(dto, PostDTO::getSources, PostDTO::setSources, sourceEntityService);
        super.handleCreateInternalReference(dto, PostDTO::getLibrary, PostDTO::setLibrary, libraryEntityService);

        return super.create(dto);
    }
}
