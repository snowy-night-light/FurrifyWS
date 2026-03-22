package ws.furrify.storage.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.file.FileDTO;

@Service
public class FileEntityCrudService extends BaseEntityCrudService<File, FileDTO> {

    @Autowired
    public FileEntityCrudService(BaseEntityRepository<File> entityRepository, BaseDTOMapper<File, FileDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
