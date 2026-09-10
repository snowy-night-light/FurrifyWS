package ws.furrify.worker.domain.worker;

import jakarta.persistence.Column;
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
    @Column
    List<String> errors;
    @Column
    List<String> warnings;

    @Column
    String log;

    @Column(nullable = false)
    WorkStatus status = WorkStatus.NOT_STARTED;

    @Column(nullable = false)
    ZonedDateTime startAt;
    @Column
    ZonedDateTime startedAt;
    @Column
    ZonedDateTime finishedAt;
}
