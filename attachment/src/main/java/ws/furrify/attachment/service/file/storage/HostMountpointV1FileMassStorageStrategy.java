package ws.furrify.attachment.service.file.storage;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.attachment.service.file.storage.thumbnail.ThumbnailGenerator;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.exception.ServiceLogicException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class HostMountpointV1FileMassStorageStrategy implements FileMassStorageStrategy {

    private final String MOUNT_POINT_PATH = "/furrify/data";

    private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    @Override
    public UploadedFileReference uploadFile(UUID id, String mimeType, File file, boolean replaceExisting) {
        try {
            Path destinationFilePath = getDestinationFilePath(id, file);
            Path destinationThumbnailFilePath = getDestinationThumbnailFilePath(id, file);

            // Ensure parent directory exists
            destinationFilePath.getParent().toFile().mkdirs();

            // Thumbnail
            File thumbnailFile = thumbnailGenerator.generateThumbnail(mimeType, file);
            Files.move(thumbnailFile, destinationThumbnailFilePath.toFile());

            // Main file
            Files.move(file, destinationFilePath.toFile());

            return UploadedFileReference.of(destinationFilePath.toUri(), destinationThumbnailFilePath.toUri(), getStorageServiceId());
        } catch (IOException e) {
            log.error("Failed to process attachment file.", e);

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage());
        }
    }

    private Path getDestinationFilePath(UUID id, File file) {
        String ext = Files.getFileExtension(file.getName());
        return Path.of(MOUNT_POINT_PATH + "/" + id + "/attachment" + (ext.isEmpty() ? "" : "." + ext));
    }

    private Path getDestinationThumbnailFilePath(UUID id, File file) {
        return Path.of(MOUNT_POINT_PATH + "/" + id + "/thumbnail.jpg");
    }

    private Path getDestinationDirectoryPath(UUID id) {
        return Path.of(MOUNT_POINT_PATH + "/" + id);
    }

    @Override
    public boolean removeFileDirectory(UUID id) {
        try {
            return FileSystemUtils.deleteRecursively(getDestinationDirectoryPath(id));
        } catch (IOException e) {
            throw new ServiceLogicException(AttachmentErrors.ATTACHMENT_FILE_DIRECTORY_REMOVE_FAILURE.getErrorMessage(id));
        }
    }

    @Override
    public String getStorageServiceId() {
        return "HostMountpointV1";
    }
}
