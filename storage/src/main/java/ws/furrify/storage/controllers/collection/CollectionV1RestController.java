package ws.furrify.storage.controllers.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.collection.request.CreateCollectionRequest;
import ws.furrify.storage.dto.collection.request.PatchCollectionRequest;


@RestController
@RequestMapping("/v1/collections")
class CollectionV1RestController extends BaseEntityRestController<Collection, CollectionDTO, CreateCollectionRequest, PatchCollectionRequest> {

    @Autowired
    public CollectionV1RestController(BaseRequestMapper<Collection, CollectionDTO, CreateCollectionRequest> requestDtoMapper, BaseEntityCrudService<Collection, CollectionDTO, PatchCollectionRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
