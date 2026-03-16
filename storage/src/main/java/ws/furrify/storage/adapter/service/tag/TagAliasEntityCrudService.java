package ws.furrify.storage.adapter.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.UserScopedEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;

@Service
public class TagAliasEntityCrudService extends UserScopedEntityCrudService<TagAlias, TagAliasDTO> {

    @Autowired
    public TagAliasEntityCrudService(UserScopedEntityRepository<TagAlias> entityRepository, BaseDTOMapper<TagAlias, TagAliasDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
