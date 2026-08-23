package ws.furrify.storage.dto.library;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.book.BookDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class LibraryDTO extends UserScopedEntityDTO<Library> {
    private String title;
    private List<PostDTO> posts;
    private List<TagDTO> tags;
    private List<ArtistDTO> artists;
    private List<CollectionDTO> collections;
    private List<BookDTO> books;
}
