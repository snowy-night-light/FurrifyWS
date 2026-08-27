package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;
import ws.furrify.storage.service.library.LibraryEntityService;

import java.util.Map;
import java.util.UUID;

@Service
public class TagEntityService extends BaseEntityCrudService<Tag, TagDTO, PatchTagRequest> {

    private final TagCategoryEntityService tagCategoryEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public TagEntityService(BaseEntityRepository<Tag> entityRepository, BaseDTOMapper<Tag, TagDTO, PatchTagRequest> dtoMapper, TagCategoryEntityService tagCategoryEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.tagCategoryEntityService = tagCategoryEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    public TagDTO create(TagDTO dto) {
        super.handleInternalReference(dto, TagDTO::getCategory, TagDTO::setCategory, tagCategoryEntityService);
        super.handleInternalReference(dto, TagDTO::getLibrary, TagDTO::setLibrary, libraryEntityService);

        super.handleUniqueConstraint(dto, Map.of(
                "name", TagDTO::getName
        ));

        return super.create(dto);
    }

    @Override
    public TagDTO patchById(UUID id, PatchTagRequest patchDto) {
        super.handleInternalReference(patchDto.getCategory(), tagCategoryEntityService);
        super.handleInternalReference(patchDto.getLibrary(), libraryEntityService);

        super.handleUniqueConstraint(patchDto, Map.of(
                "name", PatchTagRequest::getName
        ));

        return super.patchById(id, patchDto);
    }
}
