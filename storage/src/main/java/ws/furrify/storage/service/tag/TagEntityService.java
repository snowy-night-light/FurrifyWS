package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
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
        super.handleCreateInternalReference(dto, TagDTO::getCategory, TagDTO::setCategory, tagCategoryEntityService);
        super.handleCreateInternalCollectionReference(dto, TagDTO::getAliases, TagDTO::setAliases, tagAliasEntityService);

        return super.create(dto);
    }

    @Override
    public TagDTO patchById(UUID id, PatchTagRequest patchDto) {
        super.handlePatchInternalReference(patchDto.getCategory(), tagCategoryEntityService);
        super.handlePatchCollectionInternalReferences(patchDto.getAliases(), tagAliasEntityService);

        return super.patchById(id, patchDto);
    }
}
