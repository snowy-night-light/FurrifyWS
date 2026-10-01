package ws.furrify.storage.dto.artist.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.ArtistDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface ArtistRequestMapper extends BaseRequestMapper<Artist, ArtistDTO, CreateArtistRequest> {
    @Override
    ArtistDTO toDto(CreateArtistRequest createArtistRequest);
}