package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.source.Source;

import java.util.List;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media extends UserScopedEntity {

    @Column(nullable = false)
    Integer priority;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @NotNull
    File file;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Source> sources;
}