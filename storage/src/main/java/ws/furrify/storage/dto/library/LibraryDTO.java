package ws.furrify.storage.dto.library;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private List<PostDTO> posts;
    @JsonIgnore
    private List<TagDTO> tags;
    @JsonIgnore
    private List<ArtistDTO> artists;
    @JsonIgnore
    private List<CollectionDTO> collections;
    @JsonIgnore
    private List<BookDTO> books;
    private Boolean likesEnabled;
    private Boolean dislikesEnabled;
}
