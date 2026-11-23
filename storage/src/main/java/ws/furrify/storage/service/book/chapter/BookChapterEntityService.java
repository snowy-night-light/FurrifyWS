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
package ws.furrify.storage.service.book.chapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.book.chapter.BookChapterRepository;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersionRepository;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;
import ws.furrify.storage.service.book.BookEntityService;
import ws.furrify.storage.service.book.BookFileGenerationService;
import ws.furrify.storage.service.book.chapter.version.BookChapterVersionEntityService;
import ws.furrify.storage.service.source.SourceEntityService;
import ws.furrify.storage.shared.exception.StorageErrors;

import java.util.Optional;
import java.util.UUID;

@Service
public class BookChapterEntityService extends BaseEntityCrudService<BookChapter, BookChapterDTO, PatchBookChapterRequest> {

    private final BookEntityService bookEntityService;
    private final BookChapterVersionEntityService bookChapterVersionEntityService;
    private final BookChapterRepository bookChapterRepository;
    private final BookChapterVersionRepository bookChapterVersionRepository;
    private final SourceEntityService sourceEntityService;
    private final AsyncUtils asyncUtils;
    private final BookFileGenerationService bookFileGenerationService;

    @Autowired
    public BookChapterEntityService(BookChapterRepository bookChapterRepository, BaseDTOMapper<BookChapter, BookChapterDTO, PatchBookChapterRequest> dtoMapper, @Lazy BookEntityService bookEntityService, BookChapterVersionEntityService bookChapterVersionEntityService, BookChapterVersionRepository bookChapterVersionRepository, SourceEntityService sourceEntityService, AsyncUtils asyncUtils, @Lazy BookFileGenerationService bookFileGenerationService) {
        super(bookChapterRepository, dtoMapper);
        this.bookChapterRepository = bookChapterRepository;
        this.bookEntityService = bookEntityService;
        this.bookChapterVersionEntityService = bookChapterVersionEntityService;
        this.bookChapterVersionRepository = bookChapterVersionRepository;
        this.sourceEntityService = sourceEntityService;
        this.asyncUtils = asyncUtils;
        this.bookFileGenerationService = bookFileGenerationService;
    }

    @Override
    @Transactional
    protected BookChapterDTO handleCreate(BookChapterDTO dto) {
        this.handleInternalReference(dto, BookChapterDTO::getBook, BookChapterDTO::setBook, this.bookEntityService);
        this.handleInternalCollectionReferences(dto, BookChapterDTO::getSources, BookChapterDTO::setSources, sourceEntityService);

        this.checkChapterNumberForDuplicates(dto.getBook().getId(), null, dto.getChapterNumber());

        // Later calculated
        dto.setCurrentNumberOfWords(0L);

        // Refresh the book's chaptersCount (debounced, shared with the word-count update) even if
        // no version is ever created for this chapter. Inline after commit: it only schedules the
        // debounced job, so it must not take one shared-executor slot per created chapter.
        UUID bookId = dto.getBook().getId();
        asyncUtils.runAfterCommitNoTransaction(() -> this.bookEntityService.updateBookTotalWordCountAsync(bookId));

        return super.handleCreate(dto);
    }

    @Override
    @Transactional
    protected BookChapterDTO handlePatch(UUID id, PatchBookChapterRequest patchDto) {
        this.handleInternalReference(patchDto.getBook(), bookEntityService);
        this.handleCollectionInternalReferences(patchDto.getSources(), sourceEntityService);

        if (patchDto.getChapterNumber().isPresent()) {
            BookChapterDTO dto = this.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

            this.checkChapterNumberForDuplicates(dto.getBook().getId(), dto.getId(), patchDto.getChapterNumber().get());
        }

        BookChapterDTO patchedChapter = super.handlePatch(id, patchDto);

        boolean needsRegeneration = patchDto.getTitle().isPresent() || patchDto.getChapterNumber().isPresent();
        if (needsRegeneration) {
            asyncUtils.runAsyncAfterCommitNoTransaction(() -> bookFileGenerationService.executeScheduleGeneration(patchedChapter.getBook().getId()));
        }

        return patchedChapter;
    }

    @Override
    @Transactional
    protected Optional<BookChapterDTO> handleDelete(UUID id) {
        BookChapterDTO chapter = this.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        Optional<BookChapterDTO> result = super.handleDelete(id);

        UUID bookId = chapter.getBook().getId();
        asyncUtils.runAfterCommitNoTransaction(() -> this.bookEntityService.updateBookTotalWordCountAsync(bookId));
        
        return result;
    }

    @Transactional
    protected void checkChapterNumberForDuplicates(UUID bookId, UUID existingChapterId, int chapterNumber) {
        if (bookChapterRepository.existsDuplicateChapterNumber(bookId, chapterNumber, existingChapterId)) {
            throw new ServiceLogicException(StorageErrors.DUPLICATE_CHAPTER_NUMBER_EXCEPTION.getErrorMessage(chapterNumber, bookId));
        }
    }

    @Transactional
    public void updateChapterCurrentWordCountAsync(UUID chapterId) {
        if (chapterId == null) {
            return;
        }

        long wordCount = bookChapterVersionRepository.findLatestWordCountByChapterId(chapterId)
                .stream()
                .findFirst()
                .orElse(0L);

        bookChapterRepository.updateCurrentNumberOfWords(chapterId, wordCount);
        bookChapterRepository.refreshVersionsCount(chapterId);

        UUID bookId = bookChapterRepository.findBookIdByChapterId(chapterId).orElse(null);
        if (bookId != null) {
            asyncUtils.runAfterCommitNoTransaction(() -> this.bookEntityService.updateBookTotalWordCountAsync(bookId));
        }
    }

}
