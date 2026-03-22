package ws.furrify.storage.service.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;

@Service
public class SourceEntityCrudService extends BaseEntityCrudService<Source, SourceDTO> {

    @Autowired
    public SourceEntityCrudService(BaseEntityRepository<Source> entityRepository, BaseDTOMapper<Source, SourceDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
