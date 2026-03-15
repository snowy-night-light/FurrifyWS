package ws.furrify.storage.dto.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.MimeType;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.file.File;

import java.net.URI;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class FileDTO extends UserScopedEntityDTO<File> {
    private String fileName;

    private String fileHash;

    private MimeType mimeType;

    private Long fileSize;

    private URI fileUri;
    private URI thumbnailUri;

}
