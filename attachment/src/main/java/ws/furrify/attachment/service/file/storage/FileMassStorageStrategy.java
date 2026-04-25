package ws.furrify.attachment.service.file.storage;

import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.model.StrategyIntf;

import java.io.File;
import java.util.UUID;

public interface FileMassStorageStrategy extends StrategyIntf {
    UploadedFileReference uploadFile(UUID id, MimeType mimeType, File file, boolean replaceExisting);

    boolean removeFileDirectory(UUID id);

    String getStorageServiceId();
}
