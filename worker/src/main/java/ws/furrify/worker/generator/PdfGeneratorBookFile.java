package ws.furrify.worker.generator;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.BookDTO;
import org.springframework.stereotype.Component;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookChapterVersionV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.BookV1RestControllerApiClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@Slf4j
class PdfGeneratorBookFile extends EPubGeneratorBookFile {

    public PdfGeneratorBookFile(
            BookV1RestControllerApiClient bookV1RestControllerApiClient,
            BookChapterV1RestControllerApiClient bookChapterV1RestControllerApiClient,
            BookChapterVersionV1RestControllerApiClient bookChapterVersionV1RestControllerApiClient,
            AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(bookV1RestControllerApiClient, bookChapterV1RestControllerApiClient, bookChapterVersionV1RestControllerApiClient, attachmentFileV1RestControllerApiClient);
    }

    @Override
    protected String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    protected File generateFile(BookDTO bookDto, List<ChapterData> chapters, ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO task) {
        log.debug("Delegating to EPUB generator to create temporary file for PDF conversion.");
        File epubFile = super.generateFile(bookDto, chapters, task);
        
        try {
            return EbookConvertUtils.convert(epubFile, getExtension());
        } finally {
            try {
                Files.deleteIfExists(epubFile.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary EPUB file {}: {}", epubFile.getAbsolutePath(), e.getMessage());
            }
        }
    }
}
