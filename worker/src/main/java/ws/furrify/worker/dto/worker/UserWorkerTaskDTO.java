package ws.furrify.worker.dto.worker;

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
    private List<String> errors;
    private List<String> warnings;

    private String log;

    private WorkStatus status;

    private ZonedDateTime startAt;
    private ZonedDateTime startedAt;
    private ZonedDateTime finishedAt;
}
