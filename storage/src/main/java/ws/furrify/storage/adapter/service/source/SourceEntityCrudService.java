package ws.furrify.storage.adapter.service.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.UserScopedEntityCrudService;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;

@Service
public class SourceEntityCrudService extends UserScopedEntityCrudService<Source, SourceDTO> {

    @Autowired
    public SourceEntityCrudService(UserScopedEntityRepository<Source> entityRepository, BaseDTOMapper<Source, SourceDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
