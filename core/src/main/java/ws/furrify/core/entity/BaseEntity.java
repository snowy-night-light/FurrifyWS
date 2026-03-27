package ws.furrify.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecWhereStep;
import ws.furrify.core.utils.SecurityContextUtils;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseEntity {
    public final static String DEFAULT_SUBJECT = "SYSTEM";

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    UUID id;

    @Version
    long version;

    String modifiedBy;
    ZonedDateTime modifiedAt;

    @Column(nullable = false)
    String createdBy;
    @Column(nullable = false)
    ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();

        this.createdBy = SecurityContextUtils.getCurrentSubject().map(UUID::toString).orElse(DEFAULT_SUBJECT);
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = ZonedDateTime.now();
        this.modifiedBy = SecurityContextUtils.getCurrentSubject().map(UUID::toString).orElse(DEFAULT_SUBJECT);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        final BaseEntity base = (BaseEntity) o;
        return getId() != null && Objects.equals(getId(), base.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public static <ENTITY extends BaseEntity> EntitySpecWhereStep<ENTITY> specBuilder() {
        return EntitySpec.specBuilder();
    }
}
