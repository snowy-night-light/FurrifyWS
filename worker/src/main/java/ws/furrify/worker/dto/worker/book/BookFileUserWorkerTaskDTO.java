package ws.furrify.worker.dto.worker.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.UserWorkerTaskDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BookFileUserWorkerTaskDTO extends UserWorkerTaskDTO<BookFileUserWorkerTask> {

    private UUID sourceBookReferenceId;

    @Builder.Default
    private Map<String, UUID> formatReferenceIds = new HashMap<>();
}
