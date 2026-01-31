package ws.furrify.storage.dto.artist;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.PatchSourceRequestDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ArtistDTOMapper extends BaseEntityDTOMapper<Artist, ArtistDTO, PatchArtistRequestDTO> {
}