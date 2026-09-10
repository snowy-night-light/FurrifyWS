package ws.furrify.worker.dto.worker.book.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.PatchUserWorkerTaskRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatchBookFileUserWorkerTaskRequest extends PatchUserWorkerTaskRequest<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO> {
}
