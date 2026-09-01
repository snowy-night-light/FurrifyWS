package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.Min;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.source.Source;

import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(MediaEntityListener.class)
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media extends UserScopedEntity {

    @Column(nullable = false)
    @Min(0)
    @NotNull
    Integer priority;

    @Column(nullable = false)
    @NotNull
    UUID fileReferenceId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<Source> sources;
}