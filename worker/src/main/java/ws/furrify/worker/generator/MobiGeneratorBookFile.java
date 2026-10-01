package ws.furrify.worker.generator;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BookDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

@Component
@Slf4j
class MobiGeneratorBookFile extends EPubGeneratorBookFile {

    private static final Function<Long, Long> TIMEOUT_MINUTES_CALCULATOR =
            totalWords -> Math.min(1440L, 15L + (totalWords / 15000L));

    public MobiGeneratorBookFile(
            ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(attachmentFileV1RestControllerApiClient);
    }

    @Override
    protected String getContentType() {
        return "application/x-mobipocket-ebook";
    }

    @Override
    public String getExtension() {
        return "mobi";
    }

    @Value("${furrify.worker.tasks.book.renderer-process-limit:1}")
    private int rendererProcessLimit;

    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, BookFileUserWorkerTaskDTO task) {
        log.debug("Delegating to EPUB generator to create temporary file for MOBI conversion.");
        long totalWords = bookDto.getTotalWordCount() != null ? bookDto.getTotalWordCount() : 0L;
        long timeoutMinutes = TIMEOUT_MINUTES_CALCULATOR.apply(totalWords);
        File epubFile = super.generateFile(bookDto, chapters, task);
        
        try {
            return EbookConvertUtils.convert(epubFile, getExtension(), rendererProcessLimit, timeoutMinutes);
        } finally {
            try {
                Files.deleteIfExists(epubFile.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary EPUB file {}: {}", epubFile.getAbsolutePath(), e.getMessage());
            }
        }
    }
}
