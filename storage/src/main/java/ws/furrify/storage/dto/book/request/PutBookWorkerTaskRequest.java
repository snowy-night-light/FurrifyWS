package ws.furrify.storage.dto.book.request;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class PutBookWorkerTaskRequest {
    private Map<String, UUID> formatReferenceIds;
    private UUID activeWorkerTaskId;
}
