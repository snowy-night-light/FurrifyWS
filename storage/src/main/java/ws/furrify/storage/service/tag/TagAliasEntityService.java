package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

import java.util.Map;
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
        super.handleInternalReference(dto, TagAliasDTO::getTargetTag, TagAliasDTO::setTargetTag, tagEntityService);

        super.handleUniqueConstraint(dto, Map.of(
                "alias", TagAliasDTO::getAlias
        ));

        return super.create(dto);
    }

    @Override
    public TagAliasDTO patchById(UUID id, PatchTagAliasRequest patchDto) {
        super.handleInternalReference(patchDto.getTargetTag(), tagEntityService);

        super.handleUniqueConstraint(patchDto, Map.of(
                "alias", PatchTagAliasRequest::getAlias
        ));

        return super.patchById(id, patchDto);
    }
}
