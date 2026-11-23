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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import ws.furrify.core.utils.AsyncUtils;

/**
 * Lossless JPEG XL conversion with the libjxl command line tools (cjxl/djxl), plus the decoders
 * needed for formats cjxl doesn't read (ffmpeg, heif-dec) and optipng for fallback PNGs.
 * <p>
 * Every conversion is verified before it is used: a JPEG must rebuild byte for byte from the
 * JPEG XL, every other image must decode to the same pixels (every frame) as its source.
 * A conversion that fails or doesn't verify returns empty: the caller keeps the original.
 */
@Slf4j
@Component
public class JxlConverter {

    /** Ends a stuck ffmpeg after the timeout (see framePixelHashes). */
    @Autowired
    private AsyncUtils asyncUtils;

    public static final String JXL_MIME_TYPE = "image/jxl";
    public static final String JXL_EXTENSION = "jxl";

    /** Tika's type registry: aliases ("image/jpg") and specializations (APNG is a PNG) are resolved through it. */
    private static final MediaTypeRegistry TYPES =
            MimeTypes.getDefaultMimeTypes().getMediaTypeRegistry();

    /** Read by cjxl directly (with their specializations, e.g. APNG). */
    private static final Set<MediaType> CJXL_INPUT = types(
            "image/png", "image/apng", "image/vnd.mozilla.apng", "image/gif",
            "image/x-portable-pixmap", "image/x-portable-graymap", "image/x-portable-bitmap",
            "image/x-portable-anymap", "image/x-portable-arbitrarymap", "image/x-exr");
    private static final Set<MediaType> PNG = types("image/png");
    private static final Set<MediaType> WEBP = types("image/webp");
    private static final Set<MediaType> JPEG = types("image/jpeg", "image/pjpeg");
    /** Decoded by heif-dec (libheif) first. */
    private static final Set<MediaType> HEIF_INPUT = types("image/heic", "image/heif",
            "image/heic-sequence", "image/heif-sequence", "image/avif", "image/avif-sequence");
    /** Not converted: vector, already JPEG XL, or not a raster image. */
    private static final Set<MediaType> NOT_CONVERTED = types("image/svg+xml", JXL_MIME_TYPE);

    private static Set<MediaType> types(String... names) {
        return Arrays.stream(names).map(JxlConverter::canonical).filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Tika's canonical form of a type (alias resolved, parameters dropped); null when it isn't one. */
    static MediaType canonical(String mimeType) {
        MediaType type = mimeType == null ? null : MediaType.parse(mimeType.trim());
        return type == null ? null : TYPES.normalize(type.getBaseType());
    }

    /** {@code mimeType} is one of {@code kinds} or a specialization of one (Tika's type hierarchy). */
    private static boolean is(String mimeType, Set<MediaType> kinds) {
        MediaType type = canonical(mimeType);
        return type != null && kinds.stream().anyMatch(kind -> TYPES.isInstanceOf(type, kind));
    }

    /** Conversions running at once (0 = half the CPU threads); the rest wait their turn. */
    @Value("${furrify.jxl.max-concurrent:0}")
    private int maxConcurrent;
    private volatile Semaphore conversionSlots;
    private volatile int encoderThreads;

    @Value("${furrify.jxl.effort:7}")
    private int effort;
    @Value("${furrify.jxl.timeout-seconds:600}")
    private long timeoutSeconds;
    @Value("${furrify.jxl.cjxl:cjxl}")
    private String cjxl;
    @Value("${furrify.jxl.djxl:djxl}")
    private String djxl;
    @Value("${furrify.jxl.ffmpeg:ffmpeg}")
    private String ffmpeg;
    @Value("${furrify.jxl.heif-dec:heif-dec}")
    private String heifDec;
    @Value("${furrify.jxl.optipng:optipng}")
    private String optipng;
    @Value("${furrify.jxl.anim-dump:anim_dump}")
    private String animDump;
    @Value("${furrify.jxl.webpmux:webpmux}")
    private String webpmux;
    @Value("${furrify.jxl.exiftool:exiftool}")
    private String exiftool;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** A verified JPEG XL of a source image; delete {@link #jxlFile} when done with it. */
    public record Conversion(File jxlFile, boolean jpegReconstructible) {
    }

    /** Whether files of this type are converted (raster images, also those needing a decoder first). */
    public static boolean isConvertible(String mimeType) {
        MediaType type = canonical(mimeType);
        return type != null && type.getType().equals("image") && !is(mimeType, NOT_CONVERTED);
    }

    public static boolean isJpeg(String mimeType) {
        return is(mimeType, JPEG);
    }

    /**
     * Convert {@code source} (of {@code mimeType}) losslessly; empty when it can't be done or doesn't verify.
     * Only {@code furrify.jxl.max-concurrent} conversions run at once, each encoder with its share of the
     * CPU threads: dozens of encoders each using every core (an import uploads many images at once)
     * starve the whole machine and finish later than the same work done in turn.
     */
    public Optional<Conversion> convert(File source, String mimeType) {
        if (!isConvertible(mimeType)) return Optional.empty();
        Semaphore slots = conversionSlots();
        long waitStart = System.nanoTime();
        try {
            slots.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        try {
            long waitedMs = (System.nanoTime() - waitStart) / 1_000_000;
            if (waitedMs > 1000) log.debug("JPEG XL conversion of {} waited {} ms for a free slot", source.getName(), waitedMs);
            return convertNow(source, mimeType);
        } finally {
            slots.release();
        }
    }

    private Semaphore conversionSlots() {
        Semaphore slots = conversionSlots;
        if (slots == null) {
            synchronized (this) {
                if (conversionSlots == null) {
                    int cpus = Runtime.getRuntime().availableProcessors();
                    int concurrent = maxConcurrent > 0 ? maxConcurrent : Math.max(1, cpus / 2);
                    encoderThreads = Math.max(1, cpus / concurrent);
                    conversionSlots = new Semaphore(concurrent, true);
                    log.info("JPEG XL: up to {} conversions at once, {} encoder threads each", concurrent, encoderThreads);
                }
                slots = conversionSlots;
            }
        }
        return slots;
    }

    private Optional<Conversion> convertNow(File source, String mimeType) {
        String mime = mimeType.toLowerCase(Locale.ROOT);
        Path work = null;
        try {
            work = Files.createTempDirectory("jxl-");
            Path out = work.resolve("out.jxl");
            if (is(mime, JPEG)) {
                int components = jpegComponents(source.toPath());
                if (components > 0 && components != 1 && components != 3) {
                    // CMYK/YCCK (4 channels): JPEG XL can only repack 1- or 3-channel JPEGs, and turning
                    // CMYK into RGB would change its colors. Browsers show these JPEGs as they are.
                    log.debug("{} is a {}-channel JPEG (CMYK?): kept as JPEG", source.getName(), components);
                    return Optional.empty();
                }
                // lossless JPEG transcoding: the JPEG is kept bit-exact inside the JPEG XL
                run(cjxl, source.getAbsolutePath(), out.toString(), "--lossless_jpeg=1", "-e", String.valueOf(effort),
                        "--num_threads=" + encoderThreads);
                Path rebuilt = work.resolve("rebuilt.jpg");
                run(djxl, out.toString(), rebuilt.toString(), "--num_threads=" + encoderThreads);
                if (!sha256(source.toPath()).equals(sha256(rebuilt))) {
                    log.warn("JPEG XL of {} does not rebuild the original JPEG byte for byte, keeping the original", source.getName());
                    return Optional.empty();
                }
                return Optional.of(new Conversion(moveOut(out), true));
            }

            // Every other format: lossless pixels. Formats cjxl can't read go through PNG/APNG first.
            Path input;
            List<Path> webpFrames = null;
            if (is(mime, CJXL_INPUT)) {
                input = source.toPath();
            } else if (is(mime, HEIF_INPUT)) {
                input = work.resolve("decoded.png");
                run(heifDec, source.getAbsolutePath(), input.toString());
            } else if (is(mime, WEBP) && isAnimatedWebp(source.toPath())) {
                // ffmpeg can't decode animated WebP ("image data not found"): libwebp's own tools draw the frames
                webpFrames = animatedWebpFrames(source.toPath(), work);
                input = animatedWebpToApng(source.toPath(), webpFrames, work);
            } else {
                input = work.resolve("decoded.png");
                // APNG keeps every frame (multi-page TIFF and the like are written as animations)
                run(ffmpeg, "-v", "error", "-y", "-i", source.getAbsolutePath(), "-f", "apng", "-plays", "0", input.toString());
            }
            String hashFormat = pixelHashFormat(input);
            List<String> expected;
            if (webpFrames != null) {
                // libwebp's own frames, not the APNG made of them: a frame lost or changed on the way is caught
                expected = new ArrayList<>();
                for (Path frame : webpFrames) expected.addAll(framePixelHashes(frame, hashFormat));
            } else {
                expected = framePixelHashes(input, hashFormat);
            }
            boolean animatable = is(mime, CJXL_INPUT) && (!is(mime, PNG) || isAnimatedPng(input)); // APNG is often typed image/png
            List<String> actual;
            String retryReason = null;
            try {
                actual = encodeLossless(input, out, work, hashFormat);
                if (animatable && !expected.isEmpty() && actual.size() < expected.size()) {
                    // frames with no delay: JPEG XL treats a zero-duration frame as a layer of the next one
                    retryReason = expected.size() + " frames merged (no delay)";
                } else if (animatable && !expected.equals(actual)) {
                    // cjxl's own GIF/APNG reader composites some frames differently from ffmpeg (partial frames
                    // over transparency, unusual dispose/blend use): encoded from the full frames ffmpeg draws
                    retryReason = "cjxl's own reading differs from ffmpeg's";
                }
            } catch (IOException e) {
                String problem = String.valueOf(e.getMessage());
                if (problem.contains("after IEND") && is(mime, PNG)) {
                    // bytes after IEND (appended by some editors and hosts): not part of the image, browsers
                    // ignore them, cjxl refuses the whole file over them. optipng writes the PNG without them,
                    // chunks (ICC, EXIF, text) and pixels unchanged; the pixel check still applies.
                    Path trimmed = work.resolve("trimmed.png");
                    run(optipng, "-quiet", "-fix", "-o0", "-out", trimmed.toString(), input.toString());
                    log.debug("{}: bytes after the end of the PNG ignored", source.getName());
                    input = trimmed;
                    actual = encodeLossless(input, out, work, hashFormat);
                } else if (problem.contains("iCCP") && is(mime, PNG)) {
                    // a broken ICC profile chunk (a gray profile on a color image or the other way round, a
                    // truncated one): browsers ignore such a profile and show the pixels as they are, cjxl
                    // refuses the whole file over it. exiftool removes just that profile; the pixel check still applies.
                    input = withoutIccProfile(input, work.resolve("without-icc.png"));
                    log.debug("{}: broken ICC profile left out", source.getName());
                    actual = encodeLossless(input, out, work, hashFormat);
                } else if (!animatable) {
                    throw e;
                } else {
                    // GIF/APNG layouts cjxl's own reader rejects (e.g. "dispose-to-0" on partial frames)
                    retryReason = "cjxl can't read it directly";
                    actual = List.of();
                }
            }
            if (retryReason != null) {
                // ffmpeg composites every frame onto the full canvas and gives zero-delay frames the
                // 100 ms browsers use: an APNG of exactly what a browser shows, which cjxl reads fine
                Path timed = work.resolve("timed.png");
                run(ffmpeg, "-v", "error", "-y", "-i", source.getAbsolutePath(), "-f", "apng", "-plays", "0", timed.toString());
                actual = encodeLossless(timed, out, work, hashFormat);
                log.debug("JPEG XL of {} ({}) re-encoded from ffmpeg's frames ({}): {} of {} frames",
                        source.getName(), mime, retryReason, actual.size(), expected.size());
            }
            if (expected.isEmpty() || !expected.equals(actual)) {
                Path sample = keepMismatchSample(source.toPath());
                log.warn("JPEG XL of {} ({}) does not decode to the same pixels ({} vs {} frames), keeping the original. "
                                + "Source: {}; decoded: {}{}",
                        source.getName(), mime, expected.size(), actual.size(),
                        describeImage(input), describeImage(work.resolve("check.png")),
                        sample == null ? "" : "; copy kept at " + sample);
                return Optional.empty();
            }
            return Optional.of(new Conversion(moveOut(out), false));
        } catch (Exception e) {
            log.warn("JPEG XL conversion of {} ({}) failed, keeping the original: {}", source.getName(), mime, e.getMessage());
            return Optional.empty();
        } finally {
            deleteQuietly(work);
        }
    }

    /**
     * Decode a JPEG XL into its fallback format: the rebuilt original JPEG when it came from one,
     * otherwise PNG (APNG when animated), optimized losslessly with optipng when not animated (it would drop APNG frames).
     */
    public void decodeFallback(Path jxl, Path target, boolean jpeg) throws IOException, InterruptedException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp" + (jpeg ? ".jpg" : ".png"));
        try {
            conversionSlots(); // sets encoderThreads: fallbacks share the CPU with conversions
            run(djxl, jxl.toString(), tmp.toString(), "--num_threads=" + encoderThreads);
            if (!jpeg && !isAnimatedPng(tmp)) {
                try {
                    run(optipng, "-o2", "-quiet", tmp.toString()); // keeps ICC/EXIF chunks
                } catch (IOException e) {
                    log.debug("optipng failed for {}, serving the unoptimized PNG: {}", jxl, e.getMessage());
                }
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /**
     * All metadata of an image as exiftool reads it (EXIF, XMP, IPTC, ICC profile, PNG text
     * chunks, GIF comments, ...), keyed {@code Group:Tag}; binary values base64 ({@code base64:...}).
     * Embedded preview images and exiftool's own file-system facts (temporary file name, dates,
     * permissions) are left out. Empty when exiftool is missing or fails: metadata is kept on a
     * best-effort basis and never stops a conversion.
     */
    public Optional<Map<String, Object>> readMetadata(File image) {
        try {
            String json = run(exiftool, "-json", "-a", "-G1", "-b",
                    "--System:all", "--ExifTool:all", "--File:FileName",
                    "--ThumbnailImage", "--PreviewImage", "--JpgFromRaw", "--OtherImage",
                    image.getAbsolutePath());
            List<Map<String, Object>> entries = JSON.readValue(json,
                    new TypeReference<>() {
                    });
            if (entries.isEmpty()) return Optional.empty();
            Map<String, Object> metadata = new LinkedHashMap<>(entries.getFirst());
            metadata.remove("SourceFile");
            return metadata.isEmpty() ? Optional.empty() : Optional.of(metadata);
        } catch (Exception e) {
            log.debug("Metadata of {} could not be read: {}", image.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * exiftool keys (-G1) that describe how a file is encoded rather than the image, for every format:
     * file facts and values exiftool derives (File, Composite), container structure (QuickTime boxes of
     * HEIC/AVIF), the PNG/GIF/WebP headers and the TIFF/EXIF layout tags (sizes, strips, tiles, bit
     * depth, compression). Pixels, size, color, frames and their timing are all in the JPEG XL.
     */
    private static final Pattern ENCODING_DETAILS = Pattern.compile("^("
            + "(Composite|QuickTime|Track\\d*):.*"
            // file facts, but not a comment (exiftool files GIF and other comments under File:Comment)
            + "|File:(?!Comment$).*"
            + "|PNG:(ImageWidth|ImageHeight|BitDepth|ColorType|Compression|Filter|Interlace|AnimationFrames|AnimationPlays"
            // color description (cHRM, gAMA, sRGB): JPEG XL keeps it in its own color encoding; iCCP's label
            + "|WhitePointX|WhitePointY|RedX|RedY|GreenX|GreenY|BlueX|BlueY|Gamma|SRGBRendering|ProfileName"
            // print resolution (pHYs): only sets a print size, editors write 72 or 300 DPI by default
            + "|PixelsPerUnitX|PixelsPerUnitY|PixelUnits"
            // palette, transparency (pixel data, in the JPEG XL), bit-depth hint, viewer background suggestion
            + "|Palette|Transparency|SignificantBits|BackgroundColor"
            // ImageMagick's own stamps (when a conversion ran, not when the image was made; its pixel checksum) and canvas geometry
            + "|Datecreate|Datemodify|Datetimestamp|Signature|VirtualPageUnits|VirtualImageWidth|VirtualImageHeight"
            + "|ImageOffsetX|ImageOffsetY|ImageOffsetUnits)"
            // Adobe's XMP library version and Photoshop's internal document/instance ids
            + "|XMP-x:XMPToolkit|XMP-xmpMM:.*"
            + "|PNG-pHYs:.*"
            + "|GIF:(GIFVersion|ImageWidth|ImageHeight|HasColorMap|ColorResolutionDepth|BitsPerPixel|BackgroundColor"
            + "|AnimationIterations|FrameCount|Duration|TransparentColor|PixelAspectRatio)"
            + "|RIFF:(VP8Version|ImageWidth|ImageHeight|HorizontalScale|VerticalScale|WebP_Flags|AnimationLoopCount"
            + "|BackgroundColor|ExtendedWebPFlags|AlphaPreprocessing|AlphaFiltering|AlphaCompression|AlphaIsUsed|Duration|FrameCount)"
            + "|(IFD\\d+|SubIFD\\d*):(ImageWidth|ImageHeight|BitsPerSample|Compression|PhotometricInterpretation"
            + "|StripOffsets|RowsPerStrip|StripByteCounts|SamplesPerPixel|PlanarConfiguration|TileWidth|TileLength"
            + "|TileOffsets|TileByteCounts|ExtraSamples|SampleFormat|Predictor|FillOrder|ThumbnailOffset|ThumbnailLength"
            + "|SubfileType|OldSubfileType"
            // print resolution (as pHYs) and how the original compressed its YCbCr: nothing once decoded
            + "|XResolution|YResolution|ResolutionUnit|YCbCrPositioning|YCbCrSubSampling|YCbCrCoefficients)"
            + "|ExifIFD:(ExifImageWidth|ExifImageHeight|ColorSpace|ComponentsConfiguration|ExifVersion|FlashpixVersion)"
            + ")$");

    /**
     * PNG and GIF fields cjxl doesn't carry over that JPEG XL can hold as XMP: the PNG tIME chunk and the
     * "Creation Time", "Software" and "Comment" text chunks, and a GIF comment.
     */
    private static final Map<String, String> EMBEDDABLE_AS_XMP = Map.of(
            "PNG:ModifyDate", "XMP-xmp:ModifyDate",
            "PNG:CreationTime", "XMP-xmp:CreateDate",
            "PNG:Software", "XMP-xmp:CreatorTool",
            "PNG:Comment", "XMP-exif:UserComment",
            "GIF:Comment", "XMP-exif:UserComment",
            "File:Comment", "XMP-exif:UserComment");

    /**
     * Write what the JPEG XL of {@code source} can hold of {@code lostMetadata} (from {@link #metadataLostInConversion})
     * into it, before it is hashed and stored: the fields of {@link #EMBEDDABLE_AS_XMP} as XMP and, for formats
     * converted through PNG (GIF, WebP, HEIC, TIFF, ...), the source's EXIF and XMP as they are (JPEG XL has
     * boxes for both). The orientation is never copied: it would rotate pixels that are already upright or
     * are shown unrotated. Returns the source keys now in the JPEG XL, as read back from it, so they needn't be
     * kept elsewhere; a value that didn't make it (or a failed write) leaves its key out. Only metadata boxes
     * change, never the image data.
     */
    public Set<String> embedMetadata(File source, File jxl, Map<String, Object> lostMetadata, String sourceMimeType) {
        if (lostMetadata == null || lostMetadata.isEmpty()) return Set.of();
        boolean png = is(sourceMimeType, PNG);
        try {
            List<String> write = new ArrayList<>(List.of(exiftool, "-q", "-m", "-overwrite_original", "-tagsFromFile", source.getAbsolutePath()));
            if (!png) {
                // a PNG's EXIF and XMP are carried over by cjxl already
                write.addAll(List.of("-exif:all", "-xmp:all", "--Orientation", "--ThumbnailImage", "--PreviewImage"));
            }
            boolean mapped = false;
            for (String key : lostMetadata.keySet()) {
                String target = EMBEDDABLE_AS_XMP.get(key);
                if (target != null) {
                    write.add("-" + target + "<" + key);
                    mapped = true;
                }
            }
            if (png && !mapped) return Set.of();
            write.add(jxl.getAbsolutePath());
            run(write.toArray(String[]::new));

            Map<String, Object> inJxl = readMetadata(jxl).orElse(Map.of());
            Set<String> embedded = new HashSet<>();
            for (var entry : lostMetadata.entrySet()) {
                String target = EMBEDDABLE_AS_XMP.get(entry.getKey());
                boolean there = target != null
                        ? inJxl.containsKey(target)
                        : inJxl.containsKey(entry.getKey()) && String.valueOf(inJxl.get(entry.getKey())).equals(String.valueOf(entry.getValue()));
                if (there) embedded.add(entry.getKey());
            }
            return embedded;
        } catch (Exception e) {
            log.debug("Metadata of {} could not be written into its JPEG XL, kept in the database: {}", source.getName(), e.getMessage());
            return Set.of();
        }
    }

    /** exiftool groups (-G1) a PNG's JPEG XL keeps as they are: ICC profile, EXIF (all its IFDs) and XMP. */
    private static final Pattern CARRIED_OVER_FROM_PNG = Pattern.compile(
            "^(ICC_Profile|ICC-[^:]*|IFD\\d+|ExifIFD|SubIFD\\d*|GPS|InteropIFD|MakerNotes|XMP|XMP-[^:]*):.*");

    /**
     * The metadata a conversion of this file would lose, to keep in the database. None for JPEG: its
     * JPEG XL rebuilds the exact file. For PNG what JPEG XL drops (text chunks, timestamp), as it
     * keeps the ICC profile, EXIF and XMP. For formats converted through PNG (GIF, WebP, HEIC, ...) all
     * descriptive metadata (EXIF, XMP, ICC, comments), since that step may drop any of it. Encoding
     * details ({@link #ENCODING_DETAILS}) are never kept. Empty when nothing would be lost or exiftool
     * can't read the file.
     */
    public Optional<Map<String, Object>> metadataLostInConversion(File image, String mimeType) {
        if (isJpeg(mimeType)) {
            return Optional.empty();
        }
        Optional<Map<String, Object>> metadata = readMetadata(image);
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        boolean png = is(mimeType, PNG);
        Map<String, Object> lost = new LinkedHashMap<>(metadata.get());
        lost.keySet().removeIf(key -> ENCODING_DETAILS.matcher(key).matches()
                || (png && CARRIED_OVER_FROM_PNG.matcher(key).matches()));
        // the default orientation says nothing; a rotated or mirrored one is kept
        lost.entrySet().removeIf(e -> e.getKey().endsWith(":Orientation") && "Horizontal (normal)".equals(String.valueOf(e.getValue())));
        // GIMP writes this comment into every file by default: it says nothing beyond the tool
        lost.entrySet().removeIf(e -> e.getKey().endsWith(":Comment") && "Created with GIMP".equals(String.valueOf(e.getValue()).trim()));
        return lost.isEmpty() ? Optional.empty() : Optional.of(lost);
    }

    @Value("${furrify.jxl.mismatch-samples-dir:/tmp/jxl-mismatch}")
    private String mismatchSamplesDir;
    private static final int MAX_MISMATCH_SAMPLES = 50;

    /** A copy of a file whose conversion didn't verify, for looking into it later (at most 50 kept). */
    private Path keepMismatchSample(Path source) {
        if (mismatchSamplesDir == null || mismatchSamplesDir.isBlank()) return null;
        try {
            Path dir = Path.of(mismatchSamplesDir);
            Files.createDirectories(dir);
            try (var files = Files.list(dir)) {
                if (files.count() >= MAX_MISMATCH_SAMPLES) return null;
            }
            Path copy = dir.resolve(System.currentTimeMillis() + "-" + source.getFileName());
            Files.copy(source, copy, StandardCopyOption.REPLACE_EXISTING);
            return copy;
        } catch (IOException e) {
            return null;
        }
    }

    /** Pixel format and size as ffmpeg reads it, plus a PNG's header and color-related chunks. */
    private String describeImage(Path image) {
        StringBuilder sb = new StringBuilder();
        try {
            String probe = run(ffmpeg.replace("ffmpeg", "ffprobe"), "-v", "error", "-select_streams", "v:0",
                    "-show_entries", "stream=pix_fmt,width,height", "-of", "csv=p=0", image.toString());
            sb.append(probe.trim());
        } catch (Exception e) {
            sb.append("ffprobe failed");
        }
        try {
            // a PNG's header as exiftool reads it (bit depth, color type, interlacing, ICC profile)
            String png = run(exiftool, "-s", "-s", "-PNG:BitDepth", "-PNG:ColorType", "-PNG:Interlace", "-ICC_Profile:ProfileDescription",
                    image.toString()).trim().replaceAll("\\s*\\n\\s*", ", ");
            if (!png.isEmpty()) sb.append(", ").append(png);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            // nothing more to say about it
        }
        return sb.toString();
    }

    /** Encode {@code input} losslessly into {@code out}; the pixel hashes of every frame it decodes back to. */
    private List<String> encodeLossless(Path input, Path out, Path work, String hashFormat) throws IOException, InterruptedException {
        // container format (a few bytes more): metadata can be added to it afterwards (embedMetadata)
        run(cjxl, input.toString(), out.toString(), "-d", "0", "-e", String.valueOf(effort), "--num_threads=" + encoderThreads,
                "--container=1");
        Path decoded = work.resolve("check.png");
        Files.deleteIfExists(decoded);
        run(djxl, out.toString(), decoded.toString(), "--num_threads=" + encoderThreads);
        return framePixelHashes(decoded, hashFormat);
    }

    /**
     * The pixel format both sides are compared in: 8-bit RGBA for 8-bit (and palette) images, 16-bit
     * RGBA only for deeper ones. ffmpeg's palette to 16-bit RGBA conversion is inexact, so comparing a
     * palette PNG at 16 bits fails although every pixel is the same; at its own depth it is exact.
     */
    private String pixelHashFormat(Path image) {
        try {
            String pixFmt = run(ffmpeg.replace("ffmpeg", "ffprobe"), "-v", "error", "-select_streams", "v:0",
                    "-show_entries", "stream=pix_fmt", "-of", "csv=p=0", image.toString()).trim();
            return pixFmt.matches(".*(9|10|12|14|16|32|48|64|f)(le|be)?$") ? "rgba64le" : "rgba";
        } catch (Exception e) {
            return "rgba64le"; // unknown: the stricter format (can only refuse, never accept wrongly)
        }
    }

    /**
     * MD5 of every decoded frame's pixels in {@code pixFmt} ("rgba" or "rgba64le"), with the color of
     * fully transparent pixels set to 0 first. That color is invisible and decoders disagree on it: ffmpeg
     * gives a GIF's transparent pixels the palette color of the transparent index (often white), JPEG XL's
     * GIF reader gives them another. Every visible pixel, and the alpha of every pixel, must still match.
     */
    private List<String> framePixelHashes(Path image, String pixFmt) throws IOException, InterruptedException {
        String[] size = run(ffmpeg.replace("ffmpeg", "ffprobe"), "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height", "-of", "csv=p=0", image.toString()).trim().split(",");
        int width = Integer.parseInt(size[0].trim());
        int height = Integer.parseInt(size[1].trim());
        int bytesPerPixel = "rgba".equals(pixFmt) ? 4 : 8;
        long frameBytes = (long) width * height * bytesPerPixel;
        if (frameBytes <= 0 || frameBytes > Integer.MAX_VALUE - 16) {
            throw new IOException("image too large to verify: " + width + "x" + height);
        }

        Path stderr = Files.createTempFile("jxl-err-", ".txt");
        Process process = new ProcessBuilder(ffmpeg, "-v", "error", "-i", image.toString(), "-map", "0:v:0",
                "-fps_mode", "passthrough", "-pix_fmt", pixFmt, "-f", "rawvideo", "-")
                .redirectError(stderr.toFile())
                .start();
        // reading blocks on the pipe: a watchdog ends a stuck ffmpeg, which ends the read
        ScheduledFuture<?> watchdog = asyncUtils.runAsyncLaterNoTransaction(process::destroyForcibly, Duration.ofSeconds(timeoutSeconds));
        try {
            List<String> hashes = new ArrayList<>();
            byte[] frame = new byte[(int) frameBytes];
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            try (InputStream in = process.getInputStream()) {
                while (true) {
                    int read = in.readNBytes(frame, 0, frame.length);
                    if (read == 0) break;
                    if (read < frame.length) throw new IOException("ffmpeg returned a partial frame of " + image.getFileName());
                    if (bytesPerPixel == 4) {
                        for (int p = 0; p < frame.length; p += 4) {
                            if (frame[p + 3] == 0) { frame[p] = 0; frame[p + 1] = 0; frame[p + 2] = 0; }
                        }
                    } else {
                        for (int p = 0; p < frame.length; p += 8) {
                            if (frame[p + 6] == 0 && frame[p + 7] == 0) {
                                for (int k = 0; k < 6; k++) frame[p + k] = 0;
                            }
                        }
                    }
                    hashes.add(HexFormat.of().formatHex(md5.digest(frame)));
                }
            }
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS) || watchdog.isDone()) {
                process.destroyForcibly();
                throw new IOException("ffmpeg timed out after " + timeoutSeconds + "s reading " + image.getFileName());
            }
            if (process.exitValue() != 0) {
                String err = Files.readString(stderr, StandardCharsets.UTF_8).trim();
                throw new IOException("ffmpeg exited with " + process.exitValue() + (err.isEmpty() ? "" : ": " + abbreviate(err)));
            }
            return hashes;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } finally {
            watchdog.cancel(false);
            if (process.isAlive()) process.destroyForcibly();
            Files.deleteIfExists(stderr);
        }
    }

    /**
     * Color channels of a JPEG as exiftool reads them from its frame header: 1 grayscale, 3 YCbCr/RGB,
     * 4 CMYK/YCCK; -1 when unknown (no frame header, exiftool missing: cjxl then decides).
     */
    int jpegComponents(Path jpeg) {
        try {
            String components = run(exiftool, "-s3", "-File:ColorComponents", jpeg.toString()).trim();
            return components.matches("\\d+") ? Integer.parseInt(components) : -1;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return -1;
        }
    }

    /** A copy of a PNG without its ICC profile (exiftool removes the iCCP chunk, everything else stays). */
    Path withoutIccProfile(Path png, Path target) throws IOException, InterruptedException {
        Files.deleteIfExists(target); // exiftool doesn't overwrite
        run(exiftool, "-q", "-ICC_Profile:all=", "-o", target.toString(), png.toString());
        return target;
    }

    /** The frames of an animated WebP as full-canvas PNGs, drawn by libwebp's own decoder (anim_dump), in order. */
    private List<Path> animatedWebpFrames(Path webp, Path work) throws IOException, InterruptedException {
        Path frames = Files.createDirectories(work.resolve("webp-frames"));
        run(animDump, "-folder", frames.toString(), "-prefix", "frame_", webp.toString());
        try (var list = Files.list(frames)) {
            return list.filter(f -> f.getFileName().toString().endsWith(".png")).sorted().toList();
        }
    }

    /**
     * An animated WebP's frames as an APNG, timed from webpmux. Frames of 10 ms or less get 100 ms, as
     * browsers show them (JPEG XL would otherwise merge a zero-duration frame into the next).
     */
    private Path animatedWebpToApng(Path webp, List<Path> frames, Path work) throws IOException, InterruptedException {
        String info = run(webpmux, "-info", webp.toString());
        List<Integer> durations = new ArrayList<>();
        int loops = 0;
        for (String line : info.split("\n")) {
            String t = line.trim();
            if (t.matches("\\d+:\\s.*")) {
                String[] cols = t.split("\\s+");  // No.: width height alpha x_offset y_offset duration dispose blend size compression
                if (cols.length > 6) durations.add(Integer.parseInt(cols[6]));
            } else if (t.contains("Loop Count")) {
                Matcher m = Pattern.compile("Loop Count\\s*:\\s*(\\d+)").matcher(t);
                if (m.find()) loops = Integer.parseInt(m.group(1));
            }
        }
        if (frames.isEmpty() || frames.size() != durations.size()) {
            throw new IOException("animated WebP: " + frames.size() + " frames drawn but " + durations.size() + " frame timings");
        }
        return writeApng(frames, durations.stream().map(d -> d <= 10 ? 100 : d).toList(), loops, work.resolve("decoded.png"));
    }

    /**
     * An APNG of these frames (PNGs of the same size), each shown for its delay in ms, written by ffmpeg: the
     * concat demuxer gives every frame its own duration (read at millisecond precision), the APNG muxer keeps
     * them in a 1/1000 s time base and gives the last frame its own delay. Needs ffmpeg 5 or newer.
     */
    Path writeApng(List<Path> frames, List<Integer> delaysMs, int loops, Path target) throws IOException, InterruptedException {
        if (frames.isEmpty() || frames.size() != delaysMs.size()) {
            throw new IOException(frames.size() + " frames but " + delaysMs.size() + " delays");
        }
        StringBuilder list = new StringBuilder("ffconcat version 1.0\n");
        for (int i = 0; i < frames.size(); i++) {
            String file = frames.get(i).toAbsolutePath().toString().replace("'", "'\\''");
            list.append("file '").append(file).append("'\n")
                    .append("option framerate 1000\n")
                    .append("duration ").append(seconds(delaysMs.get(i))).append('\n');
        }
        Path concat = target.resolveSibling(target.getFileName() + ".ffconcat");
        Files.writeString(concat, list.toString(), StandardCharsets.UTF_8);
        run(ffmpeg, "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", concat.toString(),
                "-fps_mode", "passthrough", "-enc_time_base", "1:1000",
                "-plays", String.valueOf(loops), "-final_delay", seconds(delaysMs.getLast()),
                "-f", "apng", target.toString());
        return target;
    }

    private static String seconds(int ms) {
        return String.format(Locale.ROOT, "%.3f", Math.min(ms, 65535) / 1000.0);
    }

    /** A WebP with an animation, as webpmux reports it; false when it isn't one or webpmux can't tell. */
    boolean isAnimatedWebp(Path webp) {
        try {
            return run(webpmux, "-info", webp.toString()).contains("animation");
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        }
    }

    /** A PNG that is an animation: ffprobe reads it as APNG; false when it isn't one or ffprobe can't tell. */
    boolean isAnimatedPng(Path png) {
        try {
            return run(ffmpeg.replace("ffmpeg", "ffprobe"), "-v", "error", "-select_streams", "v:0", "-show_entries", "stream=codec_name",
                    "-of", "csv=p=0", png.toString()).trim().equals("apng");
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Run a tool; its stdout is returned, a non-zero exit or timeout is an IOException with its stderr. */
    private String run(String... command) throws IOException, InterruptedException {
        Path stdout = Files.createTempFile("jxl-out-", ".txt");
        Path stderr = Files.createTempFile("jxl-err-", ".txt");
        try {
            Process process = new ProcessBuilder(command)
                    .redirectOutput(stdout.toFile())
                    .redirectError(stderr.toFile())
                    .start();
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException(command[0] + " timed out after " + timeoutSeconds + "s");
            }
            if (process.exitValue() != 0) {
                String err = Files.readString(stderr, StandardCharsets.UTF_8).trim();
                throw new IOException(command[0] + " exited with " + process.exitValue() + (err.isEmpty() ? "" : ": " + abbreviate(err)));
            }
            return Files.readString(stdout, StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
        }
    }

    private static File moveOut(Path jxl) throws IOException {
        Path target = Files.createTempFile("converted-", "." + JXL_EXTENSION);
        Files.move(jxl, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toFile();
    }

    public static String sha256(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            for (int n; (n = in.read(buffer)) > 0; ) digest.update(buffer, 0, n);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String abbreviate(String s) {
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) return;
        try (var files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
        }
    }
}
