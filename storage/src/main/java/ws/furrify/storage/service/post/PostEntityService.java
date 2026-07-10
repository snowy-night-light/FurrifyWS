package ws.furrify.storage.service.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.request.PatchPostRequest;
import ws.furrify.storage.service.artist.ArtistEntityService;
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

    @Autowired
    public PostEntityService(BaseEntityRepository<Post> entityRepository, BaseDTOMapper<Post, PostDTO, PatchPostRequest> dtoMapper, TagEntityService tagEntityService, ArtistEntityService artistEntityService, SourceEntityService sourceEntityService, MediaEntityService mediaEntityService) {
        super(entityRepository, dtoMapper);
        this.tagEntityService = tagEntityService;
        this.artistEntityService = artistEntityService;
        this.sourceEntityService = sourceEntityService;
        this.mediaEntityService = mediaEntityService;
    }

    @Override
    public PostDTO patchById(UUID id, PatchPostRequest patchDto) {
        if (patchDto.getTags().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getTags().get()) {
                if (!this.tagEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }
        if (patchDto.getArtists().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getArtists().get()) {
                if (!this.artistEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }

        if (patchDto.getDisplayMediaList().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getDisplayMediaList().get()) {
                if (!this.mediaEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }
        if (patchDto.getAttachments().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getAttachments().get()) {
                if (!this.mediaEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

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

    @Override
    public PostDTO create(PostDTO dto) {
        if (dto.getTags() != null) {
            dto.setTags(
                    dto.getTags().stream()
                            .map(tag -> this.tagEntityService.findById(tag.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(tag.getId()))))
                            .toList()
            );
        }
        if (dto.getArtists() != null) {
            dto.setArtists(
                    dto.getArtists().stream()
                            .map(artist -> this.artistEntityService.findById(artist.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(artist.getId()))))
                            .toList()
            );
        }

        if (dto.getDisplayMediaList() != null) {
            dto.setDisplayMediaList(
                    dto.getDisplayMediaList().stream()
                            .map(media -> this.mediaEntityService.findById(media.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(media.getId()))))
                            .toList()
            );
        }
        if (dto.getAttachments() != null) {
            dto.setAttachments(
                    dto.getAttachments().stream()
                            .map(attachment -> this.mediaEntityService.findById(attachment.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(attachment.getId()))))
                            .toList()
            );
        }

        if (dto.getSources() != null) {
            dto.setSources(
                    dto.getSources().stream()
                            .map(source -> this.sourceEntityService.findById(source.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(source.getId()))))
                            .toList()
            );
        }

        return super.create(dto);
    }
}
