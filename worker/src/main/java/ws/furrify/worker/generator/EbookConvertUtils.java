package ws.furrify.worker.generator;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.UUID;

/**
 * Utility class for interacting with the Calibre ebook-convert command-line tool.
 */
@Slf4j
class EbookConvertUtils {

    /**
     * Converts a source ebook file to a target format using ebook-convert.
     *
     * @param sourceFile The source file (e.g., EPUB)
     * @param targetExtension The desired target file extension (e.g., "azw3", "mobi", "pdf")
     * @return The generated temporary target file
     * @throws RuntimeException if the conversion fails or is interrupted
     */
    public static File convert(File sourceFile, String targetExtension) {
        File targetFile = new File("/tmp/" + UUID.randomUUID() + "." + targetExtension);
        log.debug("Executing ebook-convert to generate {} file: {} -> {}", targetExtension.toUpperCase(), sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());

        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ebook-convert", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()
            );
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ebook-convert failed with exit code " + exitCode);
            }
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to convert file to " + targetExtension.toUpperCase() + " via ebook-convert", e);
        }

        return targetFile;
    }
}
