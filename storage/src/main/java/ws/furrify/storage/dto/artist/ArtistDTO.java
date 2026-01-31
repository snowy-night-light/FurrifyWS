package ws.furrify.storage.dto.artist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.source.Source;

@EqualsAndHashCode(callSuper = true)
@Data
public class ArtistDTO extends BaseEntityDTO<Artist> {
}
