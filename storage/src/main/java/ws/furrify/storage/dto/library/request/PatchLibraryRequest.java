package ws.furrify.storage.dto.library.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;

@Data
public class PatchLibraryRequest implements BasePatchEntityRequest<Library, LibraryDTO> {

    private JsonNullable<@NotBlank @Length(max = 120) String> title = JsonNullable.undefined();
}
