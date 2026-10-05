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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;
import ws.furrify.storage.dto.book.chapter.request.CreateBookChapterRequest;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;


@RestController
@RequestMapping("/v1/books/chapters")
class BookChapterV1RestController extends BaseEntityRestController<BookChapter, BookChapterDTO, CreateBookChapterRequest, PatchBookChapterRequest> {

    @Autowired
    public BookChapterV1RestController(BaseRequestMapper<BookChapter, BookChapterDTO, CreateBookChapterRequest> requestDtoMapper, BaseEntityCrudService<BookChapter, BookChapterDTO, PatchBookChapterRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
