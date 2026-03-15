package ws.furrify.storage.adapter.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.CreateTagRequest;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;


@RestController
@RequestMapping("/storage/tags")
class TagRestController extends BaseEntityRestController<Tag, TagDTO, CreateTagRequest, PatchTagRequest> {

    @Autowired
    public TagRestController(BaseRequestMapper<Tag, TagDTO, CreateTagRequest, PatchTagRequest> requestDtoMapper, BaseEntityCrudService<Tag, TagDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
