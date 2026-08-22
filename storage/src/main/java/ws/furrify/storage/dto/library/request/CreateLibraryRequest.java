package ws.furrify.storage.dto.library.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;

@Data
public class CreateLibraryRequest implements BaseCreateEntityRequest<Library, LibraryDTO> {

    @NotBlank
    @Length(max = 120)
    private String title;
}
