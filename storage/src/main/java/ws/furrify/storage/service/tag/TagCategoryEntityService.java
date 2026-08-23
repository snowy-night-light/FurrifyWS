package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;
import ws.furrify.storage.service.library.LibraryEntityService;

import java.util.UUID;

@Service
public class TagCategoryEntityService extends BaseEntityCrudService<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> {

    private final LibraryEntityService libraryEntityService;

    @Autowired
    public TagCategoryEntityService(BaseEntityRepository<TagCategory> entityRepository, BaseDTOMapper<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> dtoMapper, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    public TagCategoryDTO patchById(UUID id, PatchTagCategoryRequest patchDto) {
        this.handlePatchInternalReference(patchDto.getLibrary(), libraryEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public TagCategoryDTO create(TagCategoryDTO dto) {
        this.handleCreateInternalReference(dto, TagCategoryDTO::getLibrary, TagCategoryDTO::setLibrary, libraryEntityService);

        return super.create(dto);
    }
}
