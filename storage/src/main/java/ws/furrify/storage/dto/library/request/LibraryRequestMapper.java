package ws.furrify.storage.dto.library.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface LibraryRequestMapper extends BaseRequestMapper<Library, LibraryDTO, CreateLibraryRequest> {
    @Override
    LibraryDTO toDto(CreateLibraryRequest createLibraryRequest);
}