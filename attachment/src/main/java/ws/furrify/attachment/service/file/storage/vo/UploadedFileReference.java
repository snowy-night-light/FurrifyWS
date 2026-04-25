package ws.furrify.attachment.service.file.storage.vo;

import jakarta.persistence.Embeddable;
import lombok.*;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;

import java.net.URI;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class UploadedFileReference {

    @NonNull
    private URI fileUri;

    private URI thumbnailUri;

    @NonNull
    private FileMassStorageStrategy storageStrategy;
}