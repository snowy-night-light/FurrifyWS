package ws.furrify.storage.controllers.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.file.FileDTO;
import ws.furrify.storage.dto.file.request.CreateFileRequest;
import ws.furrify.storage.dto.file.request.PatchFileRequest;

@RestController
@RequestMapping("/v1/storage/files")
class FileV1RestController extends BaseEntityRestController<File, FileDTO, CreateFileRequest, PatchFileRequest> {

    @Autowired
    public FileV1RestController(BaseRequestMapper<File, FileDTO, CreateFileRequest, PatchFileRequest> requestDtoMapper, BaseEntityCrudService<File, FileDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
