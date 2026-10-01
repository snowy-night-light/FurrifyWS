package ws.furrify.storage.controllers.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.security.annotation.ServiceClientOnlySecured;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.CreateBookRequest;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.dto.book.request.PutBookWorkerTaskRequest;
import ws.furrify.storage.service.book.BookFileGenerationService;

import java.util.UUID;


@RestController
@RequestMapping("/v1/books")
class BookV1RestController extends BaseEntityRestController<Book, BookDTO, CreateBookRequest, PatchBookRequest> {

    private final BookFileGenerationService bookFileGenerationService;

    @Autowired
    public BookV1RestController(
            BaseRequestMapper<Book, BookDTO, CreateBookRequest> requestDtoMapper, 
            BaseEntityCrudService<Book, BookDTO, PatchBookRequest> entityCrudService, 
            BookFileGenerationService bookFileGenerationService) {
        super(requestDtoMapper, entityCrudService);
        this.bookFileGenerationService = bookFileGenerationService;
    }

    @PutMapping("/{id}/files/worker-task")
    @ServiceClientOnlySecured
    public void updateWorkerTaskInfo(@PathVariable UUID id, @RequestBody PutBookWorkerTaskRequest request) {
        bookFileGenerationService.updateWorkerTaskInfo(id, request.getFormatReferenceIds(), request.getActiveWorkerTaskId());
    }
}
