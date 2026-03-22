package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;

@Service
public class TagAliasEntityCrudService extends BaseEntityCrudService<TagAlias, TagAliasDTO> {

    @Autowired
    public TagAliasEntityCrudService(BaseEntityRepository<TagAlias> entityRepository, BaseDTOMapper<TagAlias, TagAliasDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
