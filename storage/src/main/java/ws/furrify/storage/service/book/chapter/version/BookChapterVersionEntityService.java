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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersionRepository;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;
import ws.furrify.storage.service.book.chapter.BookChapterEntityService;
import ws.furrify.storage.service.media.MediaEntityService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookChapterVersionEntityService extends BaseEntityCrudService<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> {

    private final BookChapterEntityService bookChapterEntityService;
    private final BookChapterVersionRepository bookChapterVersionRepository;
    private final AsyncUtils asyncUtils;
    private final MediaEntityService mediaEntityService;

    @Autowired
    public BookChapterVersionEntityService(BookChapterVersionRepository bookChapterVersionRepository,
                                           BaseDTOMapper<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> dtoMapper,
                                           @Lazy BookChapterEntityService bookChapterEntityService,
                                           AsyncUtils asyncUtils,
                                           MediaEntityService mediaEntityService) {
        super(bookChapterVersionRepository, dtoMapper);
        this.mediaEntityService = mediaEntityService;
        this.bookChapterVersionRepository = bookChapterVersionRepository;
        this.bookChapterEntityService = bookChapterEntityService;
        this.asyncUtils = asyncUtils;
    }

    @Override
    @Transactional
    public BookChapterVersionDTO create(BookChapterVersionDTO dto) {
        BookChapterVersionDTO createdDto = super.create(dto);
        asyncUtils.runAsyncAfterCommitNoTransaction(() -> {
            if (createdDto.getChapter() != null && createdDto.getChapter().getId() != null) {
                this.bookChapterEntityService.updateChapterCurrentWordCountAsync(createdDto.getChapter().getId());
            }
        });
        return createdDto;
    }

    @Override
    @Transactional
    public BookChapterVersionDTO patchById(UUID id, PatchBookChapterVersionRequest patchDto) {

        boolean contentChanged = patchDto != null && (
                (patchDto.getContentHtml() != null && patchDto.getContentHtml().isPresent())
                        || (patchDto.getAuthorNotesStart() != null && patchDto.getAuthorNotesStart().isPresent())
                        || (patchDto.getAuthorNotesEnd() != null && patchDto.getAuthorNotesEnd().isPresent()));
        BookChapterVersionDTO patchedDto = super.patchById(id, patchDto);

        if (contentChanged) {
            asyncUtils.runAsyncAfterCommitNoTransaction(() -> {
                if (patchedDto.getChapter() != null && patchedDto.getChapter().getId() != null) {
                    this.bookChapterEntityService.updateChapterCurrentWordCountAsync(patchedDto.getChapter().getId());
                }
            });
        }
        return patchedDto;
    }

    @Override
    @Transactional
    protected BookChapterVersionDTO handlePatch(UUID id, PatchBookChapterVersionRequest patchDto) {
        this.handleInternalReference(patchDto.getChapter(), bookChapterEntityService);
        this.handleCollectionInternalReferences(patchDto.getMedia(), mediaEntityService);

        // content and notes arrive sanitized (BookChapterVersionProcessorService)
        BookChapterVersionDTO result = super.handlePatch(id, patchDto);
        if (patchDto.getContentHtml() != null && patchDto.getContentHtml().isPresent()) {
            long wordCount = BookChapterVersionProcessorService.calculateWordCount(result.getContentHtml());
            result.setWordCount(wordCount);
            bookChapterVersionRepository.updateWordCount(id, wordCount);
        }

        return result;
    }

    /**
     * Save versions {@link BookChapterVersionProcessorService#createBulk} has prepared (sanitized, word counts):
     * resolves chapters and media, numbers the versions and stores them in one short transaction.
     */
    @Transactional
    public List<BookChapterVersionDTO> createPreparedBulk(List<BookChapterVersionDTO> dtos) {
        Map<UUID, BookChapterDTO> chapterMap = new HashMap<>();
        Map<UUID, Integer> highestVersionMap = new HashMap<>();

        for (BookChapterVersionDTO dto : dtos) {
            if (dto.getChapter() == null || dto.getChapter().getId() == null) {
                throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage("null"));
            }
            UUID chapterId = dto.getChapter().getId();

            if (!chapterMap.containsKey(chapterId)) {
                BookChapterDTO chapterDto = bookChapterEntityService.findById(chapterId)
                        .orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(chapterId)));
                chapterMap.put(chapterId, chapterDto);
            }
            dto.setChapter(chapterMap.get(chapterId));
            // saved through the base handleCreate (this class's would resolve them one by one), so references are resolved here
            this.handleInternalCollectionReferences(dto, BookChapterVersionDTO::getMedia, BookChapterVersionDTO::setMedia, mediaEntityService);
            if (dto.getMediaMissing() == null) dto.setMediaMissing(false);

            int nextVersion = highestVersionMap.computeIfAbsent(chapterId, this::getHighestChapterVersion) + 1;
            highestVersionMap.put(chapterId, nextVersion);
            dto.setChapterVersion(nextVersion);
        }

        List<BookChapterVersion> entities = dtos.stream()
                .map(super::handleCreate)
                .map(dtoMapper::toEntity)
                .toList();

        return entityRepository.saveAll(entities).stream()
                .map(dtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<BookChapterVersionDTO> patchBulk(Map<UUID, PatchBookChapterVersionRequest> patchDtos) {
        List<BookChapterVersionDTO> patchedDtos = super.patchBulk(patchDtos);

        Set<UUID> affectedChapterIds = patchedDtos.stream()
                .filter(dto -> dto.getChapter() != null && dto.getChapter().getId() != null)
                .map(dto -> dto.getChapter().getId())
                .collect(Collectors.toSet());

        affectedChapterIds.forEach(chapterId ->
                asyncUtils.runAsyncAfterCommitNoTransaction(() ->
                        this.bookChapterEntityService.updateChapterCurrentWordCountAsync(chapterId)
                )
        );

        return patchedDtos;
    }

    @Override
    @Transactional
    protected BookChapterVersionDTO handleCreate(BookChapterVersionDTO dto) {
        this.handleInternalReference(dto, BookChapterVersionDTO::getChapter, BookChapterVersionDTO::setChapter, this.bookChapterEntityService);
        this.handleInternalCollectionReferences(dto, BookChapterVersionDTO::getMedia, BookChapterVersionDTO::setMedia, mediaEntityService);
        if (dto.getMediaMissing() == null) dto.setMediaMissing(false);

        if (dto.getChapterVersion() == null) {
            int highestVersion = this.getHighestChapterVersion(dto.getChapter().getId());
            dto.setChapterVersion(highestVersion + 1);
        }

        // content, notes, word count and contentUpdatedAt are prepared by BookChapterVersionProcessorService
        return super.handleCreate(dto);
    }

    @Override
    @Transactional
    protected Optional<BookChapterVersionDTO> handleDelete(UUID id) {
        BookChapterVersionDTO version = this.findById(id).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));
        UUID chapterId = version.getChapter().getId();

        Optional<BookChapterVersionDTO> result = super.handleDelete(id);

        asyncUtils.runAsyncAfterCommitNoTransaction(() -> this.bookChapterEntityService.updateChapterCurrentWordCountAsync(chapterId));

        return result;
    }

    @Transactional
    public int getHighestChapterVersion(UUID chapterId) {
        if (chapterId == null) {
            return 0;
        }
        return bookChapterVersionRepository.findHighestChapterVersionByChapterId(chapterId);
    }
}
