package ws.furrify.storage.domain.tag.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TagCategory extends BaseEntity {
    @Column(unique = true, length = 255)
    @NotBlank
    String name;

    @Pattern(regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$")
    @NotBlank
    @Column(length = 7)
    String hexColor;
}

