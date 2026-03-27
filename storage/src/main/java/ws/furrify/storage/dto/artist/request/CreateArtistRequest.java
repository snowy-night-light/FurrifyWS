package ws.furrify.storage.dto.artist.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;

import java.util.List;

@Data
public class CreateArtistRequest implements BaseCreateEntityRequest<Artist, ArtistDTO> {

    @NotEmpty
    private List<@NotNull ArtistNickname> nicknames;

    @NotNull
    private List<@NotNull EntityIdRequest> sources;

    private EntityIdRequest avatar;
}
