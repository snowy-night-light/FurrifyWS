package ws.furrify.storage.adapter.controllers.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.file.FileRepository;
import ws.furrify.storage.dto.file.FileDTO;
import ws.furrify.storage.dto.file.FileDTOMapper;
import ws.furrify.storage.dto.file.PatchFileRequestDTO;

@RestController
@RequestMapping("/storage/files")
class FileRestController extends BaseEntityRestController<File, FileDTO, PatchFileRequestDTO> {

    @Autowired
    public FileRestController(final FileDTOMapper mapper, final FileRepository repository) {
        super(mapper, repository);
    }
}
