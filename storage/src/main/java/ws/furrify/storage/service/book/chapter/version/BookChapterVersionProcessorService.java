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
package ws.furrify.storage.service.book.chapter.version;

import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;
import ws.furrify.storage.service.book.chapter.BookChapterEntityService;
import ws.furrify.storage.shared.util.ContentHtmlSanitizerUtil;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Writes of chapter versions: the CPU-heavy part (sanitizing the HTML, counting words) runs here outside any
 * transaction, then {@link BookChapterVersionEntityService} stores the result in one short transaction
 * (through that bean's proxy). The only way versions should be written: the entity service doesn't sanitize.
 */
@Service
@RequiredArgsConstructor
public class BookChapterVersionProcessorService {

    private final BookChapterVersionEntityService entityService;
    private final BookChapterEntityService bookChapterEntityService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BookChapterVersionDTO create(BookChapterVersionDTO dto) {
        prepare(dto);
        return entityService.create(dto);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<BookChapterVersionDTO> createBulk(List<BookChapterVersionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        inParallel(dtos).forEach(BookChapterVersionProcessorService::prepare);

        List<BookChapterVersionDTO> created = entityService.createPreparedBulk(dtos);

        // committed: refresh the word counts of the affected chapters
        created.stream()
                .map(BookChapterVersionDTO::getChapter)
                .filter(Objects::nonNull)
                .map(BaseEntityDTO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(bookChapterEntityService::updateChapterCurrentWordCountAsync);
        return created;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BookChapterVersionDTO patch(UUID id, PatchBookChapterVersionRequest patch) {
        prepare(patch);
        return entityService.patchById(id, patch);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<BookChapterVersionDTO> patchBulk(Map<UUID, PatchBookChapterVersionRequest> patches) {
        if (patches == null || patches.isEmpty()) {
            return Collections.emptyList();
        }
        inParallel(patches.values()).forEach(BookChapterVersionProcessorService::prepare);
        return entityService.patchBulk(patches);
    }

    /** Sanitized content and notes (both reach readers as HTML), its word count, contentUpdatedAt unless given. */
    private static void prepare(BookChapterVersionDTO dto) {
        dto.setContentHtml(sanitizeHtml(dto.getContentHtml()));
        dto.setAuthorNotesStart(sanitizeNotes(dto.getAuthorNotesStart()));
        dto.setAuthorNotesEnd(sanitizeNotes(dto.getAuthorNotesEnd()));
        dto.setWordCount(calculateWordCount(dto.getContentHtml()));
        if (dto.getContentUpdatedAt() == null) {
            dto.setContentUpdatedAt(ZonedDateTime.now());
        }
    }

    /** The patched content and notes sanitized; new content also updates contentUpdatedAt unless given. */
    private static void prepare(PatchBookChapterVersionRequest patch) {
        if (patch == null) return;
        boolean contentPatched = patch.getContentHtml() != null && patch.getContentHtml().isPresent();
        if (contentPatched) {
            patch.setContentHtml(JsonNullable.of(sanitizeHtml(patch.getContentHtml().get())));
            if (patch.getContentUpdatedAt() == null || !patch.getContentUpdatedAt().isPresent()) {
                patch.setContentUpdatedAt(JsonNullable.of(ZonedDateTime.now()));
            }
        }
        if (patch.getAuthorNotesStart() != null && patch.getAuthorNotesStart().isPresent()) {
            patch.setAuthorNotesStart(JsonNullable.of(sanitizeNotes(patch.getAuthorNotesStart().get())));
        }
        if (patch.getAuthorNotesEnd() != null && patch.getAuthorNotesEnd().isPresent()) {
            patch.setAuthorNotesEnd(JsonNullable.of(sanitizeNotes(patch.getAuthorNotesEnd().get())));
        }
    }

    public static long calculateWordCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        long wordCount = 0;
        boolean inWord = false;
        boolean inTag = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '<') {
                if (inWord) {
                    wordCount++;
                    inWord = false;
                }
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            } else if (!inTag) {
                if (Character.isLetterOrDigit(c)) {
                    inWord = true;
                } else if (inWord) {
                    wordCount++;
                    inWord = false;
                }
            }
        }
        if (inWord) {
            wordCount++;
        }

        return wordCount;
    }

    /** Author notes are optional: none stays none. */
    private static String sanitizeNotes(String html) {
        return html == null ? null : sanitizeHtml(html);
    }

    private static String sanitizeHtml(String html) {
        return ContentHtmlSanitizerUtil.sanitize(html);
    }

    private static <T> Stream<T> inParallel(Collection<T> items) {
        return items.size() > 1 ? items.parallelStream() : items.stream();
    }
}
