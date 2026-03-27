package ws.furrify.storage.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.file.FileDTO;
import ws.furrify.storage.dto.file.request.PatchFileRequest;

@Service
public class FileEntityCrudService extends BaseEntityCrudService<File, FileDTO, PatchFileRequest> {

    @Autowired
    public FileEntityCrudService(BaseEntityRepository<File> entityRepository, BaseDTOMapper<File, FileDTO, PatchFileRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
