package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;

import java.util.UUID;

@Service
public class TagEntityService extends BaseEntityCrudService<Tag, TagDTO, PatchTagRequest> {

    private final TagCategoryEntityService tagCategoryEntityService;
    private final TagAliasEntityService tagAliasEntityService;

    @Autowired
    public TagEntityService(BaseEntityRepository<Tag> entityRepository, BaseDTOMapper<Tag, TagDTO, PatchTagRequest> dtoMapper, TagCategoryEntityService tagCategoryEntityService, TagAliasEntityService tagAliasEntityService) {
        super(entityRepository, dtoMapper);
        this.tagCategoryEntityService = tagCategoryEntityService;
        this.tagAliasEntityService = tagAliasEntityService;
    }

    @Override
    public TagDTO create(TagDTO dto) {
        if (dto.getCategory() != null) {
            dto.setCategory(
                    this.tagCategoryEntityService.findById(dto.getCategory().getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getCategory().getId())))
            );
        }
        if (dto.getAliases() != null) {
            dto.setAliases(
                    dto.getAliases().stream()
                            .map(alias -> this.tagAliasEntityService.findById(alias.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(alias.getId()))))
                            .toList()
            );
        }

        return super.create(dto);
    }

    @Override
    public TagDTO patchById(UUID id, PatchTagRequest patchDto) {
        if (patchDto.getCategory().isPresent() && !this.tagCategoryEntityService.existsById(patchDto.getCategory().get().getId())) {
            throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(patchDto.getCategory().get().getId()));
        }

        if (patchDto.getAliases().isPresent()) {
            for (EntityIdRequest entityIdRequest : patchDto.getAliases().get()) {
                if (!this.tagAliasEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }

        return super.patchById(id, patchDto);
    }
}
