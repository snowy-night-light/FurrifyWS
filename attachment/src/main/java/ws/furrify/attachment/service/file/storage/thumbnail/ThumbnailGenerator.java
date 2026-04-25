package ws.furrify.attachment.service.file.storage.thumbnail;

import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
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

    private final Set<MimeType> SUPPORTED_VIDEO_TYPES = Set.of(
            MediaType.valueOf("video/mp4"),
            MediaType.valueOf("video/webm"),
            MediaType.valueOf("video/x-matroska"), // .mkv
            MediaType.valueOf("video/quicktime"),  // .mov
            MediaType.valueOf("video/x-msvideo"),  // .avi
            MediaType.valueOf("video/x-flv"),
            MediaType.valueOf("video/mpeg"),
            MediaType.valueOf("video/ogg")
    );

    private final Set<MimeType> SUPPORTED_IMAGE_TYPES = Set.of(
            MediaType.IMAGE_PNG,
            MediaType.IMAGE_JPEG,
            MediaType.valueOf("image/bmp"),
            MediaType.valueOf("image/sgi"),
            MediaType.valueOf("image/x-sgi"),
            MediaType.valueOf("image/iff"),
            MediaType.valueOf("image/x-iff"),
            MediaType.valueOf("image/x-pcx"),
            MediaType.valueOf("image/pcx"),
            MediaType.valueOf("image/x-pict"),
            MediaType.valueOf("image/pict"),
            MediaType.valueOf("image/vnd.adobe.photoshop"),
            MediaType.valueOf("image/icns"),
            MediaType.valueOf("image/x-tga"),
            MediaType.valueOf("image/tiff"),
            MediaType.valueOf("image/vnd.wap.wbmp"),
            MediaType.valueOf("image/vnd.ms-dds"),
            MediaType.valueOf("image/webp"),
            MediaType.valueOf("image/x-xwindowdump"),
            MediaType.IMAGE_GIF
    );

    private final static short THUMBNAIL_WIDTH = 640;
    private final static short THUMBNAIL_HEIGHT = 480;
    private final static float THUMBNAIL_QUALITY = 0.7F;
    private final static String THUMBNAIL_FORMAT = "jpg";
    private final static String THUMBNAIL_FILE_PREFIX = "thumbnail";
    private final static int PART_OF_VIDEO_TO_THUMBNAIL = 3;

    public File generateThumbnail(MimeType mimeType, File file) throws IOException {
        if (SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            return generateImageThumbnail(file);
        }

        if (SUPPORTED_VIDEO_TYPES.contains(mimeType)) {
            return generateVideoThumbnail(file);
        }

        return null;
    }

    private File generateVideoThumbnail(File file) throws IOException {
        File tempFile = File.createTempFile(THUMBNAIL_FILE_PREFIX, THUMBNAIL_FORMAT);

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
        File tempFile = File.createTempFile(THUMBNAIL_FILE_PREFIX, THUMBNAIL_FORMAT);

        Thumbnails.of(file)
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .outputQuality(THUMBNAIL_QUALITY)
                .outputFormat(THUMBNAIL_FORMAT)
                .toFile(tempFile);

        return tempFile;
    }

}
