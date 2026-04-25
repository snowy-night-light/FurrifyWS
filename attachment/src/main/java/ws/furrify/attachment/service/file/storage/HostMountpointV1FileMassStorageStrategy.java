package ws.furrify.attachment.service.file.storage;

import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;

import java.io.File;
import java.util.UUID;

public class HostMountpointV1FileMassStorageStrategy implements FileMassStorageStrategy {

    @Override
    public UploadedFileReference uploadFile(MimeType mimeType, File file, boolean replaceExisting) {
        return null;
    }

    @Override
    public File downloadFile(UUID id) {
        return null;
    }

    @Override
    public boolean removeFile(UUID id) {
        return false;
    }

    @Override
    public void removeFileIfPresent(UUID id) {

    }

    @Override
    public void removeThumbnailFileIfPresent(UUID id) {

    }

    @Override
    public String getStorageServiceId() {
        return "HostMountpointV1";
    }
}
