package ws.furrify.worker.dto.worker.book.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface BookFileUserWorkerTaskRequestMapper extends BaseRequestMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, CreateBookFileUserWorkerTaskRequest> {
    @Override
    BookFileUserWorkerTaskDTO toDto(CreateBookFileUserWorkerTaskRequest createBookFileUserWorkerTaskRequest);
}