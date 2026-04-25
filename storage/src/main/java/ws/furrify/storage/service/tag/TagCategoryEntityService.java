package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;

@Service
public class TagCategoryEntityService extends BaseEntityCrudService<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> {

    @Autowired
    public TagCategoryEntityService(BaseEntityRepository<TagCategory> entityRepository, BaseDTOMapper<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
