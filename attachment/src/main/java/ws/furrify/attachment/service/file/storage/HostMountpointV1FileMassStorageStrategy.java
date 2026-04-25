package ws.furrify.attachment.service.file.storage;

import com.google.common.io.Files;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.MimeType;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.attachment.service.file.storage.thumbnail.ThumbnailGenerator;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.exception.ServiceLogicException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class HostMountpointV1FileMassStorageStrategy implements FileMassStorageStrategy {

    private final String MOUNT_POINT_PATH = "/furrify/data";

    private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    @Override
    public UploadedFileReference uploadFile(UUID id, MimeType mimeType, File file, boolean replaceExisting) {
        try {
            Path destinationFilePath = getDestinationFilePath(id, file);
            Path destinationThumbnailFilePath = getDestinationThumbnailFilePath(id, file);

            // Main file
            Files.move(file, destinationFilePath.toFile());

            // Thumbnail
            File thumbnailFile = thumbnailGenerator.generateThumbnail(mimeType, file);
            Files.move(thumbnailFile, destinationThumbnailFilePath.toFile());

            return UploadedFileReference.of(destinationFilePath.toUri(), destinationThumbnailFilePath.toUri(), getStorageServiceId());
        } catch (IOException e) {
            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage());
        }
    }

    private Path getDestinationFilePath(UUID id, File file) {
        return Path.of(MOUNT_POINT_PATH + "/" + id + "/attachment" + Files.getFileExtension(file.getName()));
    }

    private Path getDestinationThumbnailFilePath(UUID id, File file) {
        return Path.of(MOUNT_POINT_PATH + "/" + id + "/thumbnail" + Files.getFileExtension(file.getName()));
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
