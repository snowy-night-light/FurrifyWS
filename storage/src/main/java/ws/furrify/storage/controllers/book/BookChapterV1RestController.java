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
