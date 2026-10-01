package ws.furrify.storage.dto.collection;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.request.PatchCollectionRequest;
import ws.furrify.storage.dto.library.LibraryDTOMapper;
import ws.furrify.storage.dto.post.PostDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {PostDTOMapper.class, LibraryDTOMapper.class}
)
public interface CollectionDTOMapper extends BaseDTOMapper<Collection, CollectionDTO, PatchCollectionRequest> {
}