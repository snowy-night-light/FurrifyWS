package ws.furrify.storage.controllers.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.CreateLibraryRequest;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;


@RestController
@RequestMapping("/v1/libraries")
class LibraryV1RestController extends BaseEntityRestController<Library, LibraryDTO, CreateLibraryRequest, PatchLibraryRequest> {

    @Autowired
    public LibraryV1RestController(BaseRequestMapper<Library, LibraryDTO, CreateLibraryRequest> requestDtoMapper, BaseEntityCrudService<Library, LibraryDTO, PatchLibraryRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
