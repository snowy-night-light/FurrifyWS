package ws.furrify.storage.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.CreateTagAliasRequest;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

@RestController
@RequestMapping("/v1/storage/tags/aliases")
class TagAliasV1RestController extends BaseEntityRestController<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> {

    @Autowired
    public TagAliasV1RestController(BaseRequestMapper<TagAlias, TagAliasDTO, CreateTagAliasRequest> requestDtoMapper, BaseEntityCrudService<TagAlias, TagAliasDTO, PatchTagAliasRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
