package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Range;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.Source;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media extends UserScopedEntity {

    @Column(nullable = false)
    @Range(min = 0)
    @NotNull
    Integer priority;

    @Column(nullable = false)
    UUID fileReferenceId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<Source> sources;
}