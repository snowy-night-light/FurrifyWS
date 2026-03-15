package ws.furrify.storage.dto.artist;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.media.MediaDTOMapper;
import ws.furrify.storage.dto.source.SourceDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {SourceDTOMapper.class, MediaDTOMapper.class}
)
public interface ArtistDTOMapper extends BaseDTOMapper<Artist, ArtistDTO> {
    @Override
    void patchDTO(@MappingTarget ArtistDTO source, ArtistDTO patchDto);

    @Override
    Artist toEntity(ArtistDTO dto);

    @Override
    ArtistDTO toDto(Artist entity);
}