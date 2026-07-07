package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

import java.util.UUID;

@Service
public class TagAliasEntityService extends BaseEntityCrudService<TagAlias, TagAliasDTO, PatchTagAliasRequest> {

    private final TagEntityService tagEntityService;

    @Autowired
    public TagAliasEntityService(BaseEntityRepository<TagAlias> entityRepository, BaseDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequest> dtoMapper, @Lazy TagEntityService tagEntityService) {
        super(entityRepository, dtoMapper);
        this.tagEntityService = tagEntityService;
    }

    @Override
    public TagAliasDTO create(TagAliasDTO dto) {
        if (dto.getTargetTag() != null) {
            dto.setTargetTag(
                    this.tagEntityService.findById(dto.getTargetTag().getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(dto.getTargetTag().getId())))
            );
        }

        return super.create(dto);
    }

    @Override
    public TagAliasDTO patchById(UUID id, PatchTagAliasRequest patchDto) {
        if (patchDto.getTargetTag().isPresent() && !this.tagEntityService.existsById(patchDto.getTargetTag().get().getId())) {
            throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(patchDto.getTargetTag().get().getId()));
        }

        return super.patchById(id, patchDto);
    }
}
