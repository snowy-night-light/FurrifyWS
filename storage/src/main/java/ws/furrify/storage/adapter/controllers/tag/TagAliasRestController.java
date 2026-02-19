package ws.furrify.storage.adapter.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.domain.tag.alias.TagAliasRepository;
import ws.furrify.storage.dto.tag.alias.PatchTagAliasRequestDTO;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.TagAliasDTOMapper;


@RestController
@RequestMapping("/storage/tags/aliases")
class TagAliasRestController extends BaseEntityRestController<TagAlias, TagAliasDTO, PatchTagAliasRequestDTO> {

    @Autowired
    public TagAliasRestController(final TagAliasDTOMapper mapper, final TagAliasRepository repository) {
        super(mapper, repository);
    }
}
