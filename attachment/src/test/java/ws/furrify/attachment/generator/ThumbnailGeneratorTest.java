package ws.furrify.attachment.generator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.MimeType;
import ws.furrify.attachment.service.file.storage.thumbnail.ThumbnailGenerator;

import java.io.File;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ThumbnailGeneratorTest {

    private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    @ParameterizedTest()
    @MethodSource("getImageTestFiles")
    void generateImageTests(String mimetype, String filePath) {
        URL resourceUrl = getClass().getClassLoader().getResource(filePath);
        assert resourceUrl != null;

        assertDoesNotThrow(() -> thumbnailGenerator.generateThumbnail(MimeType.valueOf(mimetype), new File(resourceUrl.toURI())));
    }

    @ParameterizedTest()
    @MethodSource("getVideoTestFiles")
    void generateVideoTests(String mimetype, String filePath) {
        URL resourceUrl = getClass().getClassLoader().getResource(filePath);
        assert resourceUrl != null;

        assertDoesNotThrow(() -> thumbnailGenerator.generateThumbnail(MimeType.valueOf(mimetype), new File(resourceUrl.toURI())));
    }

    static Stream<Arguments> getImageTestFiles() {
        return Stream.of(
                Arguments.of("image/png", "files/image/example.png"),
                Arguments.of("image/jpg", "files/image/example.jpg"),
                Arguments.of("image/bmp", "files/image/example.bmp"),
                Arguments.of("image/vnd.adobe.photoshop", "files/image/example.psd"),
                Arguments.of("image/tiff", "files/image/example.tiff"),
                Arguments.of("image/webp", "files/image/example.webp"),
                Arguments.of("image/vnd.ms-dds", "files/image/example.dds"),
                Arguments.of("image/icns", "files/image/example.icns"),
                Arguments.of("image/iff", "files/image/example.iff"),
                Arguments.of("image/pict", "files/image/example.pict"),
                Arguments.of("image/sgi", "files/image/example.sgi"),
                Arguments.of("image/x-tga", "files/image/example.tga"),
                Arguments.of("image/vnd.wap.wbmp", "files/image/example.wbmp"),
                Arguments.of("image/pcx", "files/image/example.pcx"),
                Arguments.of("image/svg", "files/image/example.svg")
        );
    }

    static Stream<Arguments> getVideoTestFiles() {
        return Stream.of(
                Arguments.of("video/mp4", "files/video/example.mp4"),
                Arguments.of("video/webm", "files/video/example.webm"),
                Arguments.of("video/x-matroska", "files/video/example.mkv"),
                Arguments.of("video/quicktime", "files/video/example.mov"),
                Arguments.of("video/x-msvideo", "files/video/example.avi"),
                Arguments.of("video/x-flv", "files/video/example.flv"),
                Arguments.of("video/mpeg", "files/video/example.mpeg"),
                Arguments.of("video/ogg", "files/video/example.ogg")
        );
    }
}
