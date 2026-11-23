/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.service.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AttachmentFileDTO;
import org.openapitools.model.PagedModelAttachmentFileDTO;
import org.openapitools.model.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.furrify.core.utils.EntitySpecUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.worker.api.BookFileUserWorkerTaskV1RestControllerApiClient;
import ws.furrify.storage.domain.book.BookRepository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Removes generated book files (EPUB/HTML) that no book references any more: left behind when a
 * replaced file could not be deleted (the attachment service was down, a deletion failed...).
 * <p>
 * Generated files are recognised by their name, {@code <random uuid>.<extension>} (what the worker
 * uploads), for the extensions of the worker's generators (asked from the worker at every run, so a new
 * generator needs no change here; no run while the worker can't be asked). Files younger than {@link #GRACE} are skipped: a generation in progress has uploaded
 * them but not yet saved them on its book. Runs with the service token, so it sees every user's
 * files; several storage instances running it at once only delete the same files twice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookFileCleanupService {

    static final Duration GRACE = Duration.ofHours(2);
    private static final String GENERATED_NAME_BASE = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final int PAGE_SIZE = 200;

    private final AttachmentFileV1RestControllerApiClient attachmentClient;
    private final BookFileUserWorkerTaskV1RestControllerApiClient workerClient;
    private final BookRepository bookRepository;

    @Scheduled(initialDelay = 20 * 60 * 1000, fixedDelay = 6 * 60 * 60 * 1000)
    public void removeUnreferencedBookFiles() {
        List<String> extensions;
        try {
            List<String> answer = workerClient.bookFileUserWorkerTaskV1RestControllerExtensions().getBody();
            extensions = answer == null ? List.of() : answer.stream().filter(e -> e != null && e.matches("[A-Za-z0-9]+")).toList();
        } catch (Exception e) {
            log.warn("Book file cleanup skipped: the worker's book file extensions could not be read: {}", e.getMessage());
            return;
        }
        if (extensions.isEmpty()) return;
        Pattern generatedFileName = generatedFileName(extensions);

        String createdBefore = ZonedDateTime.now().minus(GRACE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String base = extensions.stream().map(e -> "fileName like '%." + e + "'").collect(Collectors.joining(" || ", "(", ")"))
                + " && createdAt < '" + createdBefore + "'";
        UUID cursor = null;
        int checked = 0, removed = 0, failed = 0;
        try {
            while (true) {
                String spec = base + (cursor != null ? " && id > " + cursor : "");
                Pageable pageable = new Pageable().page(0).size(PAGE_SIZE);
                pageable.setSort(List.of("id,asc")); // with the id cursor: stable while files are deleted
                PagedModelAttachmentFileDTO page = attachmentClient.attachmentFileV1RestControllerGetAllPaged(
                        pageable, EntitySpecUtils.encodeSpecToBase64(spec)).getBody();
                List<AttachmentFileDTO> files = page == null || page.getContent() == null ? List.of() : page.getContent();
                if (files.isEmpty()) break;

                Set<UUID> candidates = new HashSet<>();
                for (AttachmentFileDTO file : files) {
                    cursor = file.getId();
                    checked++;
                    if (file.getId() != null && file.getFileName() != null && generatedFileName.matcher(file.getFileName()).matches()) {
                        candidates.add(file.getId());
                    }
                }
                if (!candidates.isEmpty()) {
                    bookRepository.findReferencedFormatFiles(candidates).forEach(candidates::remove);
                    for (UUID id : candidates) {
                        try {
                            attachmentClient.attachmentFileV1RestControllerDelete(id);
                            removed++;
                        } catch (Exception e) {
                            failed++;
                            log.debug("Could not delete unreferenced book file {}: {}", id, e.getMessage());
                        }
                    }
                }
                if (files.size() < PAGE_SIZE) break;
            }
        } catch (Exception e) {
            log.warn("Book file cleanup stopped after checking {} files: {}", checked, e.getMessage());
        }
        if (removed > 0 || failed > 0) {
            log.info("Book file cleanup: {} unreferenced files removed, {} could not be removed (next run retries)", removed, failed);
        }
    }

    /** {@code <uuid>.<one of extensions>}, the name the worker gives a generated file. */
    static Pattern generatedFileName(List<String> extensions) {
        return Pattern.compile("^" + GENERATED_NAME_BASE + "\\.(" + extensions.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")$");
    }
}
