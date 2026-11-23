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
package ws.furrify.storage.controllers.book;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.CreateBookChapterVersionRequest;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;
import ws.furrify.storage.service.book.chapter.version.BookChapterVersionProcessorService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@RestController
@RequestMapping("/v1/books/chapters/versions")
class BookChapterVersionV1RestController extends BaseEntityRestController<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest, PatchBookChapterVersionRequest> {

    private final BaseRequestMapper<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest> requestMapper;
    private final BookChapterVersionProcessorService processorService;

    @Autowired
    public BookChapterVersionV1RestController(BaseRequestMapper<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest> requestDtoMapper,
                                              BaseEntityCrudService<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> entityCrudService,
                                              BookChapterVersionProcessorService processorService) {
        super(requestDtoMapper, entityCrudService);
        this.requestMapper = requestDtoMapper;
        this.processorService = processorService;
    }

    @Override
    @PostMapping(produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.CREATED)
    protected BookChapterVersionDTO save(@RequestBody @Valid CreateBookChapterVersionRequest dto) {
        return processorService.create(requestMapper.toDto(dto));
    }

    @Override
    @PatchMapping(value = "/{id}", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected BookChapterVersionDTO patch(@PathVariable UUID id, @RequestBody @Valid PatchBookChapterVersionRequest patchRequestDto) {
        return processorService.patch(id, patchRequestDto);
    }

    @Override
    @PatchMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected List<BookChapterVersionDTO> patchBulk(@RequestBody @Valid Map<UUID, PatchBookChapterVersionRequest> patchRequestDtos) {
        return processorService.patchBulk(patchRequestDtos);
    }

    @Override
    @PostMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.CREATED)
    protected List<BookChapterVersionDTO> saveBulk(@RequestBody @Valid List<CreateBookChapterVersionRequest> dtos) {
        return processorService.createBulk(dtos.stream().map(requestMapper::toDto).toList());
    }
}
