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
        super.handleCollectionInternalReferences(patchDto.getTags(), tagEntityService);
        super.handleCollectionInternalReferences(patchDto.getArtists(), artistEntityService);
        super.handleCollectionInternalReferences(patchDto.getDisplayMediaList(), mediaEntityService);
        super.handleCollectionInternalReferences(patchDto.getAttachments(), mediaEntityService);
        super.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);
        super.handleInternalReference(patchDto.getLibrary(), libraryEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public PostDTO create(PostDTO dto) {
        super.handleInternalCollectionReferences(dto, PostDTO::getTags, PostDTO::setTags, tagEntityService);
        super.handleInternalCollectionReferences(dto, PostDTO::getArtists, PostDTO::setArtists, artistEntityService);
        super.handleInternalCollectionReferences(dto, PostDTO::getDisplayMediaList, PostDTO::setDisplayMediaList, mediaEntityService);
        super.handleInternalCollectionReferences(dto, PostDTO::getAttachments, PostDTO::setAttachments, mediaEntityService);
        super.handleInternalCollectionReferences(dto, PostDTO::getSources, PostDTO::setSources, sourceEntityService);
        super.handleInternalReference(dto, PostDTO::getLibrary, PostDTO::setLibrary, libraryEntityService);

        return super.create(dto);
    }
}
