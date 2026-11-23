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
package ws.furrify.storage.domain.book;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
class BookEntityListener {

    private final AttachmentFileV1RestControllerApiClient attachmentClient;
    private final AsyncUtils asyncUtils;

    BookEntityListener(@Lazy AttachmentFileV1RestControllerApiClient attachmentClient, @Lazy AsyncUtils asyncUtils) {
        this.attachmentClient = attachmentClient;
        this.asyncUtils = asyncUtils;
    }

    @PrePersist
    public void prePersist(Book book) {
        if (book.getChaptersCount() == null) {
            book.setChaptersCount(0);
        }
    }

    /**
     * Generated book files (epub, pdf, ...) belong to the book alone: delete them from the
     * attachment service once the removal is committed (nothing is deleted on rollback). Runs for
     * every way a book is removed, including the cascade from a deleted library.
     */
    @PreRemove
    public void preRemove(Book book) {
        if (book.getFormatReferenceIds() == null || book.getFormatReferenceIds().isEmpty()) {
            return;
        }
        UUID bookId = book.getId();
        Set<UUID> attachmentIds = Set.copyOf(book.getFormatReferenceIds().values());
        asyncUtils.runAsyncAfterCommitNoTransaction(() -> attachmentIds.forEach(attachmentId -> {
            try {
                attachmentClient.attachmentFileV1RestControllerDelete(attachmentId);
            } catch (RuntimeException e) {
                log.error("Failed to delete book file attachment {} of removed book {}", attachmentId, bookId, e);
            }
        }));
    }
}
