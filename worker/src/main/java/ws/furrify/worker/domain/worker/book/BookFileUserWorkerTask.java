package ws.furrify.worker.domain.worker.book;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.UserWorkerTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookFileUserWorkerTask extends UserWorkerTask {

    @Column(nullable = false)
    UUID sourceBookReferenceId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_file_user_worker_task_format_id_map",
            joinColumns = {@JoinColumn(name = "book_file_user_work_task_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "format")
    Map<String, UUID> formatReferenceIds = new HashMap<>();
}
