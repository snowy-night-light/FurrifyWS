package ws.furrify.attachment.service.file.storage;

import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.model.StrategyIntf;

import java.io.File;
import java.util.UUID;

public interface FileMassStorageStrategy extends StrategyIntf {
    UploadedFileReference uploadFile(MimeType mimeType, File file, boolean replaceExisting);

    File downloadFile(UUID id);

    boolean removeFile(UUID id);

    void removeFileIfPresent(UUID id);
    void removeThumbnailFileIfPresent(UUID id);

    String getStorageServiceId();
}
