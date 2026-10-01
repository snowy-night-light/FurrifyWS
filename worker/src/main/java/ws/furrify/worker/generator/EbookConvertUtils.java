package ws.furrify.worker.generator;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for interacting with the Calibre ebook-convert command-line tool.
 */
@Slf4j
class EbookConvertUtils {

    private static final Semaphore CONVERT_CONCURRENCY = new Semaphore(2);

    private static final int MAX_LOG_LINES = 200;

    /**
     * Converts a source ebook file to a target format using ebook-convert.
     *
     * @param sourceFile      The source file (e.g., EPUB)
     * @param targetExtension The desired target file extension (e.g., "azw3", "mobi", "pdf")
     * @return The generated temporary target file
     * @throws RuntimeException if the conversion fails or is interrupted
     */
    public static File convert(File sourceFile, String targetExtension, int rendererProcessLimit, long timeoutMinutes) {
        File targetFile = new File("/tmp/" + UUID.randomUUID() + "." + targetExtension);
        targetFile.deleteOnExit();
        log.debug("Waiting for ebook-convert concurrency slot to generate {} file: {}", targetExtension.toUpperCase(), sourceFile.getAbsolutePath());

        try {
            CONVERT_CONCURRENCY.acquire();
            try {
                log.debug("Executing ebook-convert to generate {} file: {} -> {}", targetExtension.toUpperCase(), sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
                Process process = null;
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "xvfb-run", "-a", "ebook-convert", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()
                    );
                    processBuilder.environment().put("QTWEBENGINE_CHROMIUM_FLAGS", "--no-sandbox --disable-gpu --disable-dev-shm-usage --renderer-process-limit=" + rendererProcessLimit);
                    processBuilder.environment().put("QTWEBENGINE_DISABLE_SANDBOX", "1");
                    processBuilder.redirectErrorStream(true);
                    process = processBuilder.start();

                    Process finalProcess = process;
                    Deque<String> logBuffer = new ArrayDeque<>(MAX_LOG_LINES);

                    Thread logThread = new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                log.debug("[ebook-convert] {}", line);
                                synchronized (logBuffer) {
                                    if (logBuffer.size() >= MAX_LOG_LINES) {
                                        logBuffer.pollFirst();
                                    }
                                    logBuffer.addLast(line);
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Log stream interrupted", e);
                        }
                    });
                    logThread.setDaemon(true);
                    logThread.start();

                    boolean finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);
                    if (!finished) {
                        process.destroyForcibly();
                        throw new RuntimeException("ebook-convert timed out after " + timeoutMinutes + " minutes!");
                    }

                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        String output;
                        synchronized (logBuffer) {
                            output = String.join("\n", logBuffer);
                        }
                        log.error("ebook-convert output (last {} lines): {}", MAX_LOG_LINES, output);
                        throw new RuntimeException("ebook-convert failed with exit code " + exitCode + ". Output (last " + MAX_LOG_LINES + " lines): " + output);
                    }
                } catch (Exception e) {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException("Failed to convert file to " + targetExtension.toUpperCase() + " via ebook-convert: " + e.getMessage(), e);
                }
            } finally {
                CONVERT_CONCURRENCY.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for ebook-convert concurrency slot: " + e.getMessage(), e);
        }

        return targetFile;
    }
}
