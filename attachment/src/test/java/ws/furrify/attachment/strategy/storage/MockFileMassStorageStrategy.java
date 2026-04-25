package ws.furrify.attachment.strategy.storage;

import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;

import java.io.File;
import java.net.URI;
import java.util.UUID;

public class MockFileMassStorageStrategy implements FileMassStorageStrategy {

    @Override
    public UploadedFileReference uploadFile(UUID id, MimeType mimeType, File file, boolean replaceExisting) {
        return UploadedFileReference.of(URI.create("https://example.com/test.png"), URI.create("https://example.com/thumbnail.jpg"), getStorageServiceId());
    }

    @Override
    public boolean removeFileDirectory(UUID id) {
        return true;
    }

    @Override
    public String getStorageServiceId() {
        return "MockFileMassStorageStrategy";
    }
}
