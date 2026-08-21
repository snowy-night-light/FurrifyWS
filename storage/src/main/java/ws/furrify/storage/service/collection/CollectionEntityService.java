package ws.furrify.storage.service.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.collection.request.PatchCollectionRequest;
import ws.furrify.storage.service.post.PostEntityService;

import java.util.UUID;

@Service
public class CollectionEntityService extends BaseEntityCrudService<Collection, CollectionDTO, PatchCollectionRequest> {

    private final PostEntityService postEntityService;

    @Autowired
    public CollectionEntityService(BaseEntityRepository<Collection> entityRepository, BaseDTOMapper<Collection, CollectionDTO, PatchCollectionRequest> dtoMapper, PostEntityService postEntityService) {
        super(entityRepository, dtoMapper);
        this.postEntityService = postEntityService;
    }

    @Override
    public CollectionDTO patchById(UUID id, PatchCollectionRequest patchDto) {
        super.handlePatchCollectionInternalReferences(patchDto.getPosts(), postEntityService);

        return super.patchById(id, patchDto);
    }

    @Override
    public CollectionDTO create(CollectionDTO dto) {
        super.handleCreateInternalCollectionReference(dto, CollectionDTO::getPosts, CollectionDTO::setPosts, postEntityService);

        return super.create(dto);
    }
}
