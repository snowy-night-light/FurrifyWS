package ws.furrify.storage.dto.tag.category;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.library.LibraryDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TagCategoryDTO extends UserScopedEntityDTO<TagCategory> {
    private String name;

    private LibraryDTO library;

    private String hexColor;
}
