/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.security.annotation.ServiceClientOnlySecured;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.CreateBookFileUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;

import java.util.UUID;

// All of the methods need to be overridden for roles security to work here
@RestController
@ServiceClientOnlySecured
@RequestMapping("/v1/workers/user/books/files/generator")
public class BookFileUserWorkerTaskV1RestController extends UserWorkerTaskBaseRestController<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest, PatchBookFileUserWorkerTaskRequest> {

    @Autowired
    public BookFileUserWorkerTaskV1RestController(BaseRequestMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest> requestDtoMapper, UserWorkerTaskBaseEntityService<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }

    @Override
    public void triggerExecution(@PathVariable UUID id) {
        super.triggerExecution(id);
    }

    @Override
    public void cancel(@PathVariable UUID id) {
        super.cancel(id);
    }

    @Override
    protected ResponseEntity<BookFileUserWorkerTaskDTO> getById(UUID id) {
        return super.getById(id);
    }
    @Override
    protected Page<BookFileUserWorkerTaskDTO> getAllPaged(String specBase64, Pageable pageable) {
        return super.getAllPaged(specBase64, pageable);
    }

    @Override
    protected BookFileUserWorkerTaskDTO save(CreateBookFileUserWorkerTaskRequest dto) {
        return super.save(dto);
    }

    @Override
    protected BookFileUserWorkerTaskDTO patch(UUID id, PatchBookFileUserWorkerTaskRequest patchRequestDto) {
        return super.patch(id, patchRequestDto);
    }

    @Override
    protected void delete(UUID id) {
        super.delete(id);
    }
}
