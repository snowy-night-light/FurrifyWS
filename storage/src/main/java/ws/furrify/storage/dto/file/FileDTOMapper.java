package ws.furrify.storage.dto.file;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.file.request.PatchFileRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface FileDTOMapper extends BaseDTOMapper<File, FileDTO, PatchFileRequest> {
}