package ws.furrify.storage.adapter.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.request.CreateTagCategoryRequest;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;


@RestController
@RequestMapping("/storage/tags/categories")
class TagCategoryRestController extends BaseEntityRestController<TagCategory, TagCategoryDTO, CreateTagCategoryRequest, PatchTagCategoryRequest> {

    @Autowired
    public TagCategoryRestController(BaseRequestMapper<TagCategory, TagCategoryDTO, CreateTagCategoryRequest, PatchTagCategoryRequest> requestDtoMapper, BaseEntityCrudService<TagCategory, TagCategoryDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
