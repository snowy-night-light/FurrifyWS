package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.UserScopedEntityCrudService;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

@Service
public class TagEntityCrudService extends UserScopedEntityCrudService<Tag, TagDTO> {

    @Autowired
    public TagEntityCrudService(UserScopedEntityRepository<Tag> entityRepository, BaseDTOMapper<Tag, TagDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
