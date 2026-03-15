package ws.furrify.storage.dto.file.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.file.FileDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface FileRequestMapper extends BaseRequestMapper<File, FileDTO, CreateFileRequest, PatchFileRequest> {
    @Override
    FileDTO toDto(PatchFileRequest patchDto);

    @Override
    FileDTO toDto(CreateFileRequest createFileRequest);
}
