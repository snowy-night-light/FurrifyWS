package ws.furrify.storage.dto.artist.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.dto.artist.ArtistDTO;

import java.util.List;

@Data
public class PatchArtistRequest implements BasePatchEntityRequest<Artist, ArtistDTO> {

    private JsonNullable<List<@NotNull ArtistNickname>> nicknames = JsonNullable.undefined();

    private JsonNullable<List<@NotNull EntityIdRequest>> sources = JsonNullable.undefined();

    private JsonNullable<EntityIdRequest> avatar = JsonNullable.undefined();
}
