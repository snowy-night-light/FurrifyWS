package ws.furrify.worker.dto.worker.book;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;

import java.util.HashMap;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BookFileUserWorkerTaskDTO extends UserWorkerTaskDTO<BookFileUserWorkerTask> {

    private HashMap<String, UUID> formatReferenceIds;

    private UUID sourceBookReferenceId;
}
