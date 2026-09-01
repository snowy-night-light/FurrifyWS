package ws.furrify.worker.domain.worker;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;

import java.time.ZonedDateTime;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserWorkerTask extends UserScopedEntity {
    List<String> errors;
    List<String> warnings;

    String log;

    WorkStatus status = WorkStatus.NOT_STARTED;

    ZonedDateTime startAt;
    ZonedDateTime startedAt;
    ZonedDateTime finishedAt;
}
