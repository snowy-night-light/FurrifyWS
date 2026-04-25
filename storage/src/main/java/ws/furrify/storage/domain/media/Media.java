package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
    Integer priority;

    @NotNull
    UUID fileId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Source> sources;
}