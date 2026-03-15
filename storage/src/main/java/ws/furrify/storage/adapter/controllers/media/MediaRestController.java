package ws.furrify.storage.adapter.controllers.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.dto.media.MediaDTO;
import ws.furrify.storage.dto.media.request.CreateMediaRequest;
import ws.furrify.storage.dto.media.request.PatchMediaRequest;

@RestController
@RequestMapping("/storage/media")
class MediaRestController extends BaseEntityRestController<Media, MediaDTO, CreateMediaRequest, PatchMediaRequest> {

    @Autowired
    public MediaRestController(BaseRequestMapper<Media, MediaDTO, CreateMediaRequest, PatchMediaRequest> requestDtoMapper, BaseEntityCrudService<Media, MediaDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
