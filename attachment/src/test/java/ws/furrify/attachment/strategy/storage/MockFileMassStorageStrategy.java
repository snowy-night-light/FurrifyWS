package ws.furrify.attachment.strategy.hash.storage;

import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class MockFileMassStorageStrategy implements FileMassStorageStrategy {
    @Override
    public UploadedFileReference uploadFile(MimeType mimeType, File file, boolean replaceExisting) {
        return UploadedFileReference.of(URI.create("https://example.com/test.png"), URI.create("https://example.com/thumbnail.jpg"), getStorageServiceId());
    }

    @Override
    public File downloadFile(UUID id) {
        return new File(Objects.requireNonNull(getClass().getResource("files/example.png")).getFile());
    }

    @Override
    public boolean removeFile(UUID id) {
        return true;
    }

    @Override
    public void removeFileIfPresent(UUID id) {

    }

    @Override
    public void removeThumbnailFileIfPresent(UUID id) {

    }

    @Override
    public String getStorageServiceId() {
        return "MockFileMassStorageStrategy";
    }
}
