package ws.furrify.worker.dto.worker.book;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface BookFileUserWorkerTaskDTOMapper extends BaseDTOMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> {
}