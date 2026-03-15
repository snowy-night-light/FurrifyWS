package ws.furrify.storage.domain.tag;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.domain.tag.category.TagCategory;

import java.util.List;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag extends UserScopedEntity {
    @Column(unique = true, length = 64)
    @NotBlank
    String name;

    @OneToMany(cascade = CascadeType.REMOVE)
    @ToString.Exclude
    List<TagAlias> aliases;

    @ManyToOne
    TagCategory category;
}
