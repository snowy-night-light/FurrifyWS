package ws.furrify.storage.adapter.controllers.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.dto.source.PatchSourceRequestDTO;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.SourceDTOMapper;


@RestController
@RequestMapping("/sources")
class SourceRestController extends BaseEntityRestController<Source, SourceDTO, PatchSourceRequestDTO> {

    @Autowired
    public SourceRestController(final SourceDTOMapper mapper, final SourceRepository repository) {
        super(mapper, repository);
    }
}
