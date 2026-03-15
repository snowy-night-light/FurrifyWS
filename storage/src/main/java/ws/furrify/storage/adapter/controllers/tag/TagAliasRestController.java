package ws.furrify.storage.adapter.controllers.tag;

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
@RequestMapping("/storage/tags/aliases")
class TagAliasRestController extends BaseEntityRestController<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> {

    @Autowired
    public TagAliasRestController(BaseRequestMapper<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> requestDtoMapper, BaseEntityCrudService<TagAlias, TagAliasDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
