package ws.furrify.storage.dto.collection.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface CollectionRequestMapper extends BaseRequestMapper<Collection, CollectionDTO, CreateCollectionRequest> {
    @Override
    CollectionDTO toDto(CreateCollectionRequest createCollectionRequest);
}