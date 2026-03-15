package ws.furrify.storage.dto.file;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.file.File;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface FileDTOMapper extends BaseDTOMapper<File, FileDTO> {
}