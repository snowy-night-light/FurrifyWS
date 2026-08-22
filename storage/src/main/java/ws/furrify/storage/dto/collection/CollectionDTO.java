package ws.furrify.storage.dto.collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.post.PostDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CollectionDTO extends UserScopedEntityDTO<Collection> {
    private String title;
    private List<PostDTO> posts;
    private LibraryDTO library;
}
