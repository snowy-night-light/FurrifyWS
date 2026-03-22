package ws.furrify.storage.service.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;

@Service
public class MediaEntityCrudService extends BaseEntityCrudService<Media, MediaDTO> {

    @Autowired
    public MediaEntityCrudService(BaseEntityRepository<Media> entityRepository, BaseDTOMapper<Media, MediaDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
