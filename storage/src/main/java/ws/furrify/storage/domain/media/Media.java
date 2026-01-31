package ws.furrify.storage.domain.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.source.Source;

import java.util.Set;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media extends BaseEntity {

    @Column(nullable = false)
    Integer priority;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @NotNull
    File file;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Set<Source> sources;
}