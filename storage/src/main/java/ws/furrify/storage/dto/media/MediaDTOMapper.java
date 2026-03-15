package ws.furrify.storage.dto.media;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.file.FileDTOMapper;
import ws.furrify.storage.dto.source.SourceDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {FileDTOMapper.class, SourceDTOMapper.class}
)
public interface MediaDTOMapper extends BaseDTOMapper<Media, MediaDTO> {
    @Override
    void patchDTO(@MappingTarget MediaDTO source, MediaDTO patchDto);

    @Override
    Media toEntity(MediaDTO dto);

    @Override
    MediaDTO toDto(Media entity);
}