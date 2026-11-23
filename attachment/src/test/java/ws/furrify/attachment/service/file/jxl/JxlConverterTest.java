/*
 * furrify-attachment-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.attachment.service.file.jxl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.zip.CRC32;
import org.springframework.core.task.SyncTaskExecutor;
import ws.furrify.core.utils.AsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JxlConverterTest {

    @Test
    void convertibleTypes() {
        assertTrue(JxlConverter.isConvertible("image/jpeg"));
        assertTrue(JxlConverter.isConvertible("image/png"));
        assertTrue(JxlConverter.isConvertible("image/gif"));
        assertTrue(JxlConverter.isConvertible("image/webp"));
        assertTrue(JxlConverter.isConvertible("image/heic"));
        assertFalse(JxlConverter.isConvertible("image/svg+xml"));
        assertFalse(JxlConverter.isConvertible("image/jxl"));
        assertFalse(JxlConverter.isConvertible("application/zip"));
        assertFalse(JxlConverter.isConvertible(null));
        assertTrue(JxlConverter.isJpeg("image/JPEG"));
        assertTrue(JxlConverter.isJpeg("image/jpeg; charset=binary"), "parameters don't matter");
        assertFalse(JxlConverter.isJpeg("image/png"));
        assertTrue(JxlConverter.isConvertible("image/vnd.mozilla.apng"));
        assertFalse(JxlConverter.isConvertible("not a type"));
    }

    @Test
    void pngWithBytesAfterItsEndConverts(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg") && onPath("ffprobe") && onPath("optipng"), "tools not installed");
        JxlConverter converter = toolConverter();
        Path png = dir.resolve("a.png");
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        image.setRGB(3, 3, 0xFF8800);
        ImageIO.write(image, "png", png.toFile());
        Files.write(png, "appended junk".getBytes(StandardCharsets.US_ASCII), StandardOpenOption.APPEND);

        assertTrue(converter.convert(png.toFile(), "image/png").isPresent(), "the image converts, the bytes after it are ignored");
    }

    @Test
    void iccProfileIsLeftOut(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("exiftool"), "exiftool not installed");
        JxlConverter converter = toolConverter();
        Path png = dir.resolve("a.png");
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", png.toFile());
        Path icc = Files.write(dir.resolve("srgb.icc"), ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData());
        Path withIcc = dir.resolve("icc.png");
        assertEquals(0, new ProcessBuilder("exiftool", "-q", "-ICC_Profile<=" + icc, "-o", withIcc.toString(), png.toString())
                .inheritIO().start().waitFor());
        assertTrue(contains(withIcc, "iCCP"), "test setup: the PNG has a profile");

        Path without = converter.withoutIccProfile(withIcc, dir.resolve("without.png"));

        assertFalse(contains(without, "iCCP"), "the profile is gone");
        assertTrue(contains(without, "IDAT"), "the image stays");
    }

    @Test
    void framesAreWrittenAsApng(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("ffmpeg") && onPath("ffprobe"), "ffmpeg not installed");
        JxlConverter converter = toolConverter();
        Path a = dir.resolve("a.png");
        Path b = dir.resolve("b.png");
        ImageIO.write(new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB), "png", a.toFile());
        BufferedImage red = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
        red.setRGB(1, 1, 0xFFFF0000);
        ImageIO.write(red, "png", b.toFile());

        Path apng = converter.writeApng(List.of(a, b), List.of(100, 250), 0, dir.resolve("anim.png"));

        assertTrue(converter.isAnimatedPng(apng));
        Process probe = new ProcessBuilder("ffprobe", "-v", "error", "-select_streams", "v:0",
                "-show_entries", "packet=duration_time", "-of", "csv=p=0", apng.toString()).redirectErrorStream(true).start();
        List<String> durations = new String(probe.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .lines().map(String::trim).filter(l -> !l.isEmpty()).toList();
        assertEquals(List.of(0.1, 0.25), durations.stream().map(Double::parseDouble).toList(), "every frame keeps its own delay");
    }

    /** An animated WebP converts through libwebp's frames; needs img2webp, anim_dump, webpmux and the JPEG XL tools. */
    @Test
    void animatedWebpConverts(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg") && onPath("ffprobe")
                && onPath("img2webp") && onPath("anim_dump") && onPath("webpmux"), "tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng", "webpmux"}) ReflectionTestUtils.setField(converter, f, f);
        ReflectionTestUtils.setField(converter, "animDump", "anim_dump");
        Path a = dir.resolve("a.png");
        Path b = dir.resolve("b.png");
        BufferedImage one = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        BufferedImage two = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) { one.setRGB(x, y, x * 16 << 16); two.setRGB(x, y, y * 16 << 8); }
        ImageIO.write(one, "png", a.toFile());
        ImageIO.write(two, "png", b.toFile());
        Path webp = dir.resolve("anim.webp");
        assertEquals(0, new ProcessBuilder("img2webp", "-loop", "0", "-lossless", "-d", "100", a.toString(), b.toString(), "-o", webp.toString())
                .inheritIO().start().waitFor());
        assertTrue(converter.isAnimatedWebp(webp));

        var conversion = converter.convert(webp.toFile(), "image/webp").orElseThrow(() -> new AssertionError("conversion didn't verify"));
        Path fallback = dir.resolve("fallback.png");
        converter.decodeFallback(conversion.jxlFile().toPath(), fallback, false);
        assertTrue(converter.isAnimatedPng(fallback), "the fallback stays animated");
    }

    @Test
    void jpegChannelsAreRead(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("exiftool"), "exiftool not installed");
        JxlConverter converter = toolConverter();
        Path rgb = dir.resolve("rgb.jpg");
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "jpg", rgb.toFile());
        assertEquals(3, converter.jpegComponents(rgb));
        Path gray = dir.resolve("gray.jpg");
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY), "jpg", gray.toFile());
        assertEquals(1, converter.jpegComponents(gray));
        // the same header claiming 4 channels, as a CMYK JPEG's does
        byte[] bytes = Files.readAllBytes(rgb);
        for (int i = 2; i + 9 < bytes.length; i++) {
            if ((bytes[i] & 0xFF) == 0xFF && ((bytes[i + 1] & 0xFF) == 0xC0 || (bytes[i + 1] & 0xFF) == 0xC2)) {
                bytes[i + 9] = 4;
                break;
            }
        }
        Path cmyk = dir.resolve("cmyk.jpg");
        Files.write(cmyk, bytes);
        assertEquals(4, converter.jpegComponents(cmyk));
        // no frame header: unknown, cjxl decides
        assertEquals(-1, converter.jpegComponents(Files.write(dir.resolve("empty.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8})));
    }

    @Test
    void animatedPngIsRecognised(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("ffmpeg") && onPath("ffprobe"), "ffmpeg not installed");
        JxlConverter converter = toolConverter();
        Path still = dir.resolve("still.png");
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB), "png", still.toFile());
        assertFalse(converter.isAnimatedPng(still));
        Path animated = dir.resolve("animated.png");
        assertEquals(0, new ProcessBuilder("ffmpeg", "-v", "error", "-f", "lavfi", "-i", "testsrc=size=8x8:rate=10", "-frames:v", "2",
                "-plays", "0", "-f", "apng", animated.toString()).inheritIO().start().waitFor());
        assertTrue(converter.isAnimatedPng(animated));
    }

    /** Needs cjxl, djxl and ffmpeg (present in the attachment image); skipped elsewhere. */
    @Test
    void jpegAndPngRoundTripLosslessly(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg"), "JPEG XL tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "heifDec", "optipng"}) {
            ReflectionTestUtils.setField(converter, f, f.equals("heifDec") ? "heif-dec" : f);
        }
        BufferedImage image = new BufferedImage(64, 48, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 64; x++) for (int y = 0; y < 48; y++) image.setRGB(x, y, (x * 4) << 16 | (y * 5) << 8 | (x ^ y));

        File jpeg = dir.resolve("a.jpg").toFile();
        ImageIO.write(image, "jpg", jpeg);
        var jpegConversion = converter.convert(jpeg, "image/jpeg").orElseThrow();
        assertTrue(jpegConversion.jpegReconstructible());
        Path rebuilt = dir.resolve("rebuilt.jpg");
        converter.decodeFallback(jpegConversion.jxlFile().toPath(), rebuilt, true);
        assertEquals(JxlConverter.sha256(jpeg.toPath()), JxlConverter.sha256(rebuilt), "the original JPEG is rebuilt byte for byte");

        File png = dir.resolve("a.png").toFile();
        ImageIO.write(image, "png", png);
        var pngConversion = converter.convert(png, "image/png").orElseThrow();
        assertFalse(pngConversion.jpegReconstructible());
        Path decoded = dir.resolve("decoded.png");
        converter.decodeFallback(pngConversion.jxlFile().toPath(), decoded, false);
        BufferedImage back = ImageIO.read(decoded.toFile());
        for (int x = 0; x < 64; x++) for (int y = 0; y < 48; y++) {
            assertEquals(image.getRGB(x, y) & 0xFFFFFF, back.getRGB(x, y) & 0xFFFFFF, "pixel " + x + "," + y);
        }
    }

    /** A GIF whose frames have no delay keeps every frame (JPEG XL would merge zero-duration frames). */
    @Test
    void gifFramesWithoutDelayAreKept(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg"), "JPEG XL tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng"}) ReflectionTestUtils.setField(converter, f, f);
        Path gif = dir.resolve("a.gif");
        new ProcessBuilder("ffmpeg", "-v", "error", "-f", "lavfi", "-i", "testsrc=size=32x32:rate=5", "-frames:v", "5", gif.toString())
                .inheritIO().start().waitFor();
        byte[] bytes = Files.readAllBytes(gif);
        for (int i = 0; i + 5 < bytes.length; i++) { // every Graphic Control Extension: delay 0
            if (bytes[i] == 0x21 && bytes[i + 1] == (byte) 0xF9 && bytes[i + 2] == 4) { bytes[i + 4] = 0; bytes[i + 5] = 0; }
        }
        Files.write(gif, bytes);

        var conversion = converter.convert(gif.toFile(), "image/gif").orElseThrow();
        Path fallback = dir.resolve("fallback.png");
        converter.decodeFallback(conversion.jxlFile().toPath(), fallback, false);
        assertTrue(converter.isAnimatedPng(fallback), "the fallback stays animated");
    }

    /** A palette PNG with partly transparent entries converts (ffmpeg's palette to 16-bit RGBA is inexact). */
    @Test
    void palettePngWithTransparencyConverts(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg") && onPath("ffprobe"), "JPEG XL tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng"}) ReflectionTestUtils.setField(converter, f, f);
        Path png = dir.resolve("palette.png");
        try (var in = getClass().getClassLoader().getResourceAsStream("files/image/palette-alpha.png")) {
            Files.copy(Objects.requireNonNull(in), png);
        }
        assertTrue(converter.convert(png.toFile(), "image/png").isPresent(), "a lossless conversion of a palette PNG verifies");
    }

    /** An animated GIF with a transparent background converts and its fallback stays animated. */
    @Test
    void animatedGifWithTransparencyConverts(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg") && onPath("ffprobe"), "JPEG XL tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng"}) ReflectionTestUtils.setField(converter, f, f);
        Path gif = dir.resolve("animated.gif");
        try (var in = getClass().getClassLoader().getResourceAsStream("files/image/animated-transparent.gif")) {
            Files.copy(Objects.requireNonNull(in), gif);
        }
        var conversion = converter.convert(gif.toFile(), "image/gif").orElseThrow(() -> new AssertionError("conversion didn't verify"));
        Path fallback = dir.resolve("fallback.png");
        converter.decodeFallback(conversion.jxlFile().toPath(), fallback, false);
        assertTrue(converter.isAnimatedPng(fallback), "the fallback stays animated");
    }

    /** A PNG's tIME chunk ends up in its JPEG XL as xmp:ModifyDate instead of the database. */
    @Test
    void pngModifyDateIsWrittenIntoTheJxl(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("cjxl") && onPath("djxl") && onPath("ffmpeg") && onPath("ffprobe") && onPath("exiftool"), "tools not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng", "exiftool"}) ReflectionTestUtils.setField(converter, f, f);
        Path png = dir.resolve("a.png");
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", png.toFile());
        // tIME chunk: 2011-12-08 11:58:58
        byte[] data = {0x07, (byte) 0xDB, 12, 8, 11, 58, 58};
        byte[] bytes = Files.readAllBytes(png);
        CRC32 crc = new CRC32();
        crc.update("tIME".getBytes(StandardCharsets.ISO_8859_1));
        crc.update(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes, 0, 33);
        out.write(ByteBuffer.allocate(4).putInt(data.length).array());
        out.write("tIME".getBytes(StandardCharsets.ISO_8859_1));
        out.write(data);
        out.write(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
        out.write(bytes, 33, bytes.length - 33);
        Files.write(png, out.toByteArray());

        var lost = converter.metadataLostInConversion(png.toFile(), "image/png").orElseThrow();
        assertTrue(lost.containsKey("PNG:ModifyDate"), lost.toString());
        var conversion = converter.convert(png.toFile(), "image/png").orElseThrow();
        var embedded = converter.embedMetadata(png.toFile(), conversion.jxlFile(), lost, "image/png");
        assertTrue(embedded.contains("PNG:ModifyDate"), "the date is in the JPEG XL now");
        var inJxl = converter.readMetadata(conversion.jxlFile()).orElseThrow();
        assertTrue(String.valueOf(inJxl.get("XMP-xmp:ModifyDate")).startsWith("2011:12:08 11:58:58"), inJxl.toString());
    }

    /** Needs exiftool (present in the attachment image); skipped elsewhere. */
    @Test
    void pngTextChunksAreReadIntoMetadata(@TempDir Path dir) throws Exception {
        assumeTrue(onPath("exiftool"), "exiftool not installed");
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "exiftool", "exiftool");
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 60L);
        // a PNG with a tEXt chunk "Comment" = "hello" (one of the chunks JPEG XL drops)
        Path png = dir.resolve("a.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", png.toFile());
        byte[] bytes = Files.readAllBytes(png);
        byte[] data = "Comment\0hello".getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        crc.update("tEXt".getBytes(StandardCharsets.ISO_8859_1));
        crc.update(data);
        chunk.write(ByteBuffer.allocate(4).putInt(data.length).array());
        chunk.write("tEXt".getBytes(StandardCharsets.ISO_8859_1));
        chunk.write(data);
        chunk.write(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes, 0, 33); // signature + IHDR
        out.write(chunk.toByteArray());
        out.write(bytes, 33, bytes.length - 33);
        Files.write(png, out.toByteArray());

        var metadata = converter.readMetadata(png.toFile()).orElseThrow();
        assertEquals("hello", String.valueOf(metadata.get("PNG:Comment")), metadata.toString());
        assertFalse(metadata.containsKey("System:FileName"), "file-system facts are left out");

        // kept for PNG: the text chunk (JPEG XL drops it), not what JPEG XL carries over
        var lost = converter.metadataLostInConversion(png.toFile(), "image/png").orElseThrow();
        assertEquals("hello", String.valueOf(lost.get("PNG:Comment")));
        assertTrue(lost.keySet().stream().noneMatch(k -> k.startsWith("File:") || k.startsWith("ICC") || k.startsWith("XMP")
                || k.startsWith("Composite:") || k.equals("PNG:ImageWidth") || k.equals("PNG:BitDepth")), lost.toString());
        // a GIF keeps its comment but none of its header
        Path gif = dir.resolve("a.gif");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "gif", gif.toFile());
        assertEquals(0, new ProcessBuilder("exiftool", "-q", "-overwrite_original", "-Comment=hi", gif.toString()).inheritIO().start().waitFor(),
                "exiftool could not write the GIF comment");
        var gifRead = converter.readMetadata(gif.toFile()).orElseThrow(() -> new AssertionError("exiftool read nothing from the GIF"));
        String commentKey = gifRead.keySet().stream().filter(k -> k.endsWith(":Comment")).findFirst()
                .orElseThrow(() -> new AssertionError("exiftool wrote no comment into the GIF: " + gifRead));
        var gifLost = converter.metadataLostInConversion(gif.toFile(), "image/gif")
                .orElseThrow(() -> new AssertionError("the GIF comment was dropped; exiftool read " + gifRead));
        assertEquals("hi", String.valueOf(gifLost.get(commentKey)), gifLost.toString());
        assertTrue(gifLost.keySet().stream().noneMatch(k -> k.equals("GIF:ImageWidth") || k.equals("GIF:GIFVersion")
                || (k.startsWith("File:") && !k.equals("File:Comment")) || k.startsWith("Composite:")), gifLost.toString());
        // a plain PNG (header only) keeps nothing
        Path plain = dir.resolve("plain.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", plain.toFile());
        assertTrue(converter.metadataLostInConversion(plain.toFile(), "image/png").isEmpty(),
                () -> converter.readMetadata(plain.toFile()).toString());
        // nothing for JPEG: its JPEG XL rebuilds the exact file
        assertTrue(converter.metadataLostInConversion(png.toFile(), "image/jpeg").isEmpty());
    }

    /** A converter with the tools on the PATH under their usual names. */
    private static JxlConverter toolConverter() {
        JxlConverter converter = newConverter();
        ReflectionTestUtils.setField(converter, "effort", 3);
        ReflectionTestUtils.setField(converter, "timeoutSeconds", 120L);
        for (String f : new String[]{"cjxl", "djxl", "ffmpeg", "optipng", "webpmux", "exiftool"}) ReflectionTestUtils.setField(converter, f, f);
        ReflectionTestUtils.setField(converter, "animDump", "anim_dump");
        return converter;
    }

    private static boolean contains(Path file, String text) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1).contains(text);
    }

    /** A converter outside Spring: its AsyncUtils runs delayed tasks on the calling side, no transaction. */
    private static JxlConverter newConverter() {
        JxlConverter converter = new JxlConverter();
        ReflectionTestUtils.setField(converter, "asyncUtils", new AsyncUtils(null, new SyncTaskExecutor()));
        return converter;
    }

    private static boolean onPath(String tool) {
        for (String dir : System.getenv().getOrDefault("PATH", "").split(File.pathSeparator)) {
            if (new File(dir, tool).canExecute()) return true;
        }
        return false;
    }
}
