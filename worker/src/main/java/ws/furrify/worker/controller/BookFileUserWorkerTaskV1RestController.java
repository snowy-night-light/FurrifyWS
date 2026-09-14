package ws.furrify.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/v1/user/workers/books/files/generator")
public class BookFileUserWorkerTaskV1RestController extends UserWorkerTaskBaseRestController<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest, PatchBookFileUserWorkerTaskRequest> {

    @Autowired
    public BookFileUserWorkerTaskV1RestController(BaseRequestMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest> requestDtoMapper, UserWorkerTaskBaseEntityService<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }

    @Override
    public void triggerExecution(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        super.triggerExecution(id);
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
