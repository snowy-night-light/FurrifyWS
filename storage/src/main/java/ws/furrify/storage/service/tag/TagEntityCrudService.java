package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;

@Service
public class TagEntityCrudService extends BaseEntityCrudService<Tag, TagDTO> {

    @Autowired
    public TagEntityCrudService(BaseEntityRepository<Tag> entityRepository, BaseDTOMapper<Tag, TagDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
