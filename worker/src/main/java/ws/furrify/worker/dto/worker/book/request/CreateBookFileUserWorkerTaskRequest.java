package ws.furrify.worker.dto.worker.book.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.request.CreateUserWorkerTaskRequest;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateBookFileUserWorkerTaskRequest extends CreateUserWorkerTaskRequest<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO> {

    @NotNull
    private UUID sourceBookReferenceId;

}
