package ws.furrify.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.UUID;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserScopedEntity extends BaseEntity {
    @Column(nullable = false)
    UUID ownerId;

    @Override
    protected void onCreate() {
        super.onCreate();

        this.ownerId = SecurityContextUtils.getCurrentSubject().orElseThrow(() -> new RuntimeException("User context was not found when attempting to create user scoped entity."));
    }
}
