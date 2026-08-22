package ws.furrify.storage.service.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;

import java.util.UUID;

@Service
public class LibraryEntityService extends BaseEntityCrudService<Library, LibraryDTO, PatchLibraryRequest> {

    @Autowired
    public LibraryEntityService(BaseEntityRepository<Library> entityRepository, BaseDTOMapper<Library, LibraryDTO, PatchLibraryRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }

    @Override
    public LibraryDTO patchById(UUID id, PatchLibraryRequest patchDto) {
        return super.patchById(id, patchDto);
    }

    @Override
    public LibraryDTO create(LibraryDTO dto) {
        return super.create(dto);
    }
}
