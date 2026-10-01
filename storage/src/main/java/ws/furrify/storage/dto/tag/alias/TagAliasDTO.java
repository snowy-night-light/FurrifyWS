package ws.furrify.storage.dto.tag.alias;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.TagDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TagAliasDTO extends UserScopedEntityDTO<TagAlias> {
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TagDTO targetTag;

    private String alias;
}