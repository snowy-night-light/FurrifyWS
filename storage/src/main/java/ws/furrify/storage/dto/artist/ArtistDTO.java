package ws.furrify.storage.dto.artist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.source.SourceDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ArtistDTO extends UserScopedEntityDTO<Artist> {

    private List<ArtistNickname> nicknames;

    private List<SourceDTO> sources;

    private MediaDTO avatar;
}
