package ws.furrify.storage.service.artist;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.UniqueConstraintViolationException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecJoinStep;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.media.MediaEntityService;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.shared.util.ContentHtmlSanitizerUtil;

import java.util.List;
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
        super.handleInternalReference(dto, ArtistDTO::getAvatar, ArtistDTO::setAvatar, mediaEntityService);
        super.handleInternalReference(dto, ArtistDTO::getLibrary, ArtistDTO::setLibrary, libraryEntityService);
        super.handleInternalCollectionReferences(dto, ArtistDTO::getSources, ArtistDTO::setSources, sourceEntityService);

        // Sanitize bio html
        dto.setBioHtml(sanitizeHtml(dto.getBioHtml()));

        checkNicknameUniqueness(dto.getNicknames(), null);

        return super.create(dto);
    }

    @Override
    public ArtistDTO patchById(UUID id, PatchArtistRequest patchDto) {
        super.handleInternalReference(patchDto.getAvatar(), mediaEntityService);
        super.handleInternalReference(patchDto.getLibrary(), libraryEntityService);
        super.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        // Sanitize bio html
        if (patchDto.getBioHtml().isPresent()) {
            patchDto.setBioHtml(JsonNullable.of(sanitizeHtml(patchDto.getBioHtml().get())));
        }

        if (patchDto.getNicknames() != null && patchDto.getNicknames().isPresent()) {
            checkNicknameUniqueness(patchDto.getNicknames().get(), id);
        }

        return super.patchById(id, patchDto);
    }

    private String sanitizeHtml(String html) {
        return ContentHtmlSanitizerUtil.sanitize(html);
    }

    private void checkNicknameUniqueness(List<ArtistNickname> nicknames, UUID recordId) {
        if (nicknames == null || nicknames.isEmpty()) return;
        EntitySpecJoinStep<Artist> specBuilder = null;
        for (ArtistNickname nickname : nicknames) {
            if (specBuilder == null) {
                specBuilder = EntitySpec.<Artist>specBuilder()
                        .where("nicknames.nickname", EntitySpec.specEquals(nickname.getNickname()));
            } else {
                specBuilder = specBuilder.or()
                        .where("nicknames.nickname", EntitySpec.specEquals(nickname.getNickname()));
            }
        }
        if (specBuilder != null) {
            var finalSpec = specBuilder.build();
            if (recordId != null) {
                finalSpec = EntitySpec.<Artist>specBuilder()
                        .where("id", EntitySpec.specNotEquals(recordId))
                        .and(finalSpec)
                        .build();
            }
            if (entityRepository.count(finalSpec) > 0) {
                throw new UniqueConstraintViolationException(
                        Errors.UNIQUE_CONSTRAINT_VIOLATION.getErrorMessage("nicknames.nickname")
                );
            }
        }
    }
}
