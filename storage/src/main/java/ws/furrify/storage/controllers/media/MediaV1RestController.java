package ws.furrify.storage.controllers.media;

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
@RequestMapping("/v1/storage/media")
class MediaV1RestController extends BaseEntityRestController<Media, MediaDTO, CreateMediaRequest, PatchMediaRequest> {

    @Autowired
    public MediaV1RestController(BaseRequestMapper<Media, MediaDTO, CreateMediaRequest> requestDtoMapper, BaseEntityCrudService<Media, MediaDTO, PatchMediaRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
