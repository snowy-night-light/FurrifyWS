package ws.furrify.storage.dto.artist.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class CreateArtistRequest implements BaseCreateEntityRequest<Artist, ArtistDTO> {

    private String externalId;

    @NotEmpty
    private List<@NotNull ArtistNickname> nicknames;

    private List<@NotNull EntityIdRequest> sources;

    @PositiveOrZero
    @NotNull
    private Integer followersCount;

    private String bioHtml;
    private EntityIdRequest avatar;

    private EntityIdRequest library;

    private ZonedDateTime externalUpdatedAt;
}
