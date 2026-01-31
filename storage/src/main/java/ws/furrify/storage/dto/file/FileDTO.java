package ws.furrify.storage.dto.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.MimeType;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.file.File;

import java.net.URI;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileDTO extends BaseEntityDTO<File> {
    private String fileName;

    private String fileHash;

    private MimeType mimeType;

    private Long fileSize;

    private URI fileUri;
    private URI thumbnailUri;

}
