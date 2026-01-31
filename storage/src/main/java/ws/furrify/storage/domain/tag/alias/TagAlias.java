package ws.furrify.storage.domain.tag.alias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.storage.domain.tag.Tag;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TagAlias extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    Tag targetTag;

    @Column(unique = true, length = 64)
    @NotBlank
    String alias;
}
