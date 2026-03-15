package ws.furrify.storage.dto.artist;

import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.media.MediaDTOMapper;
import ws.furrify.storage.dto.source.SourceDTOMapper;

import java.util.List;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {SourceDTOMapper.class, MediaDTOMapper.class}
)
public interface ArtistDTOMapper extends BaseDTOMapper<Artist, ArtistDTO> {
}