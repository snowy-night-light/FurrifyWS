package ws.furrify.worker.dto.worker;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.worker.domain.worker.WorkStatus;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UserWorkerTaskDTO<ENTITY extends UserScopedEntity> extends UserScopedEntityDTO<ENTITY> {
    @ElementCollection
    @CollectionTable(name="errors", joinColumns=@JoinColumn(name="task_id"))
    @Column()
    private List<String> errors;
    @ElementCollection
    @CollectionTable(name="warnings", joinColumns=@JoinColumn(name="task_id"))
    @Column()
    private List<String> warnings;

    @Column(columnDefinition = "TEXT")
    private String log;

    @Column(nullable = false)
    private WorkStatus status = WorkStatus.NOT_STARTED;

    @Column(nullable = false)
    private ZonedDateTime startAt;
    @Column
    private ZonedDateTime startedAt;
    @Column
    private ZonedDateTime finishedAt;
}
