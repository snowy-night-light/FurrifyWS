package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.UserScopedEntityCrudService;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;

@Service
public class TagCategoryEntityCrudService extends UserScopedEntityCrudService<TagCategory, TagCategoryDTO> {

    @Autowired
    public TagCategoryEntityCrudService(UserScopedEntityRepository<TagCategory> entityRepository, BaseDTOMapper<TagCategory, TagCategoryDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
