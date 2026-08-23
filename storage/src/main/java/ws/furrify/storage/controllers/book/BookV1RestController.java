package ws.furrify.storage.controllers.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.request.CreateBookRequest;
import ws.furrify.storage.dto.book.request.PatchBookRequest;


@RestController
@RequestMapping("/v1/books")
class BookV1RestController extends BaseEntityRestController<Book, BookDTO, CreateBookRequest, PatchBookRequest> {

    @Autowired
    public BookV1RestController(BaseRequestMapper<Book, BookDTO, CreateBookRequest> requestDtoMapper, BaseEntityCrudService<Book, BookDTO, PatchBookRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
