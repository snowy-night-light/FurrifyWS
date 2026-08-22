package ws.furrify.storage.dto.library;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.artist.ArtistDTOMapper;
import ws.furrify.storage.dto.collection.CollectionDTOMapper;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.storage.dto.post.PostDTOMapper;
import ws.furrify.storage.dto.tag.TagDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {PostDTOMapper.class, TagDTOMapper.class, ArtistDTOMapper.class, CollectionDTOMapper.class}
)
public interface LibraryDTOMapper extends BaseDTOMapper<Library, LibraryDTO, PatchLibraryRequest> {
}