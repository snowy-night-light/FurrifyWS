package ws.furrify.storage.dto.artist;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.dto.artist.request.PatchArtistRequest;
import ws.furrify.storage.dto.library.LibraryDTOMapper;
import ws.furrify.storage.dto.media.MediaDTOMapper;
import ws.furrify.storage.dto.source.SourceDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {SourceDTOMapper.class, MediaDTOMapper.class, LibraryDTOMapper.class}
)
public interface ArtistDTOMapper extends BaseDTOMapper<Artist, ArtistDTO, PatchArtistRequest> {
}