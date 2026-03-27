package ws.furrify.storage.service.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.request.PatchSourceRequest;

@Service
public class SourceEntityCrudService extends BaseEntityCrudService<Source, SourceDTO, PatchSourceRequest> {

    @Autowired
    public SourceEntityCrudService(BaseEntityRepository<Source> entityRepository, BaseDTOMapper<Source, SourceDTO, PatchSourceRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
