package ws.furrify.storage.controllers.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.request.CreateBookChapterVersionRequest;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;


@RestController
@RequestMapping("/v1/books/chapters/versions")
class BookChapterVersionV1RestController extends BaseEntityRestController<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest, PatchBookChapterVersionRequest> {

    @Autowired
    public BookChapterVersionV1RestController(BaseRequestMapper<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest> requestDtoMapper, BaseEntityCrudService<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
