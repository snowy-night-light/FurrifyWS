package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.CreateBookFileUserWorkerTaskRequest;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;


@RestController
@Secured("ROLE_service_client")
@RequestMapping("/v1/user/workers/books/files/generator")
class BookFileUserWorkerTaskV1RestController extends UserWorkerTaskBaseRestController<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest, PatchBookFileUserWorkerTaskRequest> {

    @Autowired
    public BookFileUserWorkerTaskV1RestController(BaseRequestMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest> requestDtoMapper, UserWorkerTaskBaseEntityService<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
