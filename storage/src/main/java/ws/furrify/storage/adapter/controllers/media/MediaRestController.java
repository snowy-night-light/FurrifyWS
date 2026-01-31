package ws.furrify.storage.adapter.controllers.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.media.MediaRepository;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.media.MediaDTOMapper;
import ws.furrify.storage.dto.media.PatchMediaRequestDTO;

@RestController
@RequestMapping("/media")
class MediaRestController extends BaseEntityRestController<Media, MediaDTO, PatchMediaRequestDTO> {

    @Autowired
    public MediaRestController(final MediaDTOMapper mapper, final MediaRepository repository) {
        super(mapper, repository);
    }
}
