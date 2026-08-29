package ws.furrify.attachment.service.file.storage.thumbnail;

import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.core.exception.ServiceLogicException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

public class ThumbnailGenerator {

    private final Set<String> SUPPORTED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/x-matroska", // .mkv
            "video/quicktime",  // .mov
            "video/x-msvideo",  // .avi
            "video/x-flv",
            "video/mpeg",
            "video/ogg"
    );

    private final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/bmp",
            "image/sgi",
            "image/x-sgi",
            "image/iff",
            "image/x-iff",
            "image/x-pcx",
            "image/pcx",
            "image/x-pict",
            "image/pict",
            "image/vnd.adobe.photoshop",
            "image/icns",
            "image/x-tga",
            "image/tiff",
            "image/vnd.wap.wbmp",
            "image/vnd.ms-dds",
            "image/webp",
            "image/svg",
            "image/svg+xml",
            "image/gif"
    );

    private final static short THUMBNAIL_WIDTH = 640;
    private final static short THUMBNAIL_HEIGHT = 480;
    private final static float THUMBNAIL_QUALITY = 0.7F;
    private final static String THUMBNAIL_FORMAT = "jpg";
    private final static String THUMBNAIL_FILE_PREFIX = "thumbnail";
    private final static int PART_OF_VIDEO_TO_THUMBNAIL = 3;

    public File generateThumbnail(String mimeType, File file) throws IOException {
        if (SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            return generateImageThumbnail(file);
        }

        if (SUPPORTED_VIDEO_TYPES.contains(mimeType)) {
            return generateVideoThumbnail(file);
        }

        return null;
    }

    private File generateVideoThumbnail(File file) throws IOException {
        File tempFile = File.createTempFile(THUMBNAIL_FILE_PREFIX, "." + THUMBNAIL_FORMAT);

        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            frameGrabber.start();
            frameGrabber.setVideoFrameNumber(frameGrabber.getLengthInFrames() / PART_OF_VIDEO_TO_THUMBNAIL);

            Frame frame = frameGrabber.grabImage();
            BufferedImage image = converter.convert(frame);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);

            frameGrabber.stop();

            Files.write(tempFile.toPath(), output.toByteArray());

            return tempFile;
        } catch (Exception e) {
            throw new ServiceLogicException(AttachmentErrors.VIDEO_FRAME_EXTRACTION_FAILED.getErrorMessage(file.getName()));
        }
    }

    private File generateImageThumbnail(File file) throws IOException {
        File tempFile = File.createTempFile(THUMBNAIL_FILE_PREFIX, "." + THUMBNAIL_FORMAT);

        Thumbnails.of(file)
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .outputQuality(THUMBNAIL_QUALITY)
                .outputFormat(THUMBNAIL_FORMAT)
                .toFile(tempFile);

        return tempFile;
    }

}
