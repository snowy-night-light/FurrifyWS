package ws.furrify.storage.adapter.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.TagRepository;
import ws.furrify.storage.dto.tag.PatchTagRequestDTO;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.TagDTOMapper;


@RestController
@RequestMapping("/tags")
class TagRestController extends BaseEntityRestController<Tag, TagDTO, PatchTagRequestDTO> {

    @Autowired
    public TagRestController(final TagDTOMapper mapper, final TagRepository repository) {
        super(mapper, repository);
    }
}
