package ws.furrify.storage.controllers.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.request.CreateSourceRequest;
import ws.furrify.storage.dto.source.request.PatchSourceRequest;


@RestController
@RequestMapping("/v1/storage/sources")
class SourceV1RestController extends BaseEntityRestController<Source, SourceDTO, CreateSourceRequest, PatchSourceRequest> {

    @Autowired
    public SourceV1RestController(BaseRequestMapper<Source, SourceDTO, CreateSourceRequest, PatchSourceRequest> requestDtoMapper, BaseEntityCrudService<Source, SourceDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
