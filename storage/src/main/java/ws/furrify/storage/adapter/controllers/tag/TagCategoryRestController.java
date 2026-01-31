package ws.furrify.storage.adapter.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.domain.tag.category.TagCategoryRepository;
import ws.furrify.storage.dto.tag.category.PatchTagCategoryRequestDTO;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;


@RestController
@RequestMapping("/tags/categories")
class TagCategoryRestController extends BaseEntityRestController<TagCategory, TagCategoryDTO, PatchTagCategoryRequestDTO> {

    @Autowired
    public TagCategoryRestController(final TagCategoryDTOMapper mapper, final TagCategoryRepository repository) {
        super(mapper, repository);
    }
}
