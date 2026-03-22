package ws.furrify.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
public class UserScopedEntity extends BaseEntity {
    @Column(nullable = false)
    UUID ownerId;

    @Override
    protected void onCreate() {
        super.onCreate();

        if (this.ownerId == null) {
            this.ownerId = SecurityContextUtils.getCurrentSubject().orElseThrow(() -> new RuntimeException("User context was not found when attempting to create user scoped entity."));
        }
    }
}
