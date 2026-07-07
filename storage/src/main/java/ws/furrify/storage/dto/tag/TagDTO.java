package ws.furrify.storage.dto.tag;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TagDTO extends UserScopedEntityDTO<Tag> {
    @Pattern(regexp = "^[a-z0-9 ]+$")
    private String name;

    private List<TagAliasDTO> aliases;

    private TagCategoryDTO category;
}