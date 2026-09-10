package ws.furrify.worker.service.worker.book;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.BookFileUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;
import ws.furrify.worker.generator.BookFileWorkerGenerator;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static ws.furrify.worker.domain.worker.WorkStatus.IN_PROGRESS;

@Service
@Slf4j
public class BookFileUserWorkerTaskEntityService extends UserWorkerTaskBaseEntityService<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> {

    private final List<BookFileWorkerGenerator> generators;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @Autowired
    public BookFileUserWorkerTaskEntityService(BaseEntityRepository<BookFileUserWorkerTask> entityRepository, BaseDTOMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> dtoMapper, AsyncUtils asyncUtils, List<BookFileWorkerGenerator> generators, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient) {
        super(entityRepository, dtoMapper, asyncUtils);
        this.generators = generators;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
    }


    @Override
    public BookFileUserWorkerTaskDTO create(BookFileUserWorkerTaskDTO dto) {
        try {
            if (attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(dto.getSourceBookReferenceId()).getBody() == null) {
                throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getSourceBookReferenceId()));
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getSourceBookReferenceId()));
        }

        return super.create(dto);
    }

    @Override
    @Transactional
    public BookFileUserWorkerTaskDTO patchById(UUID id, PatchBookFileUserWorkerTaskRequest patchDto) {
        BookFileUserWorkerTaskDTO bookFileUserWorkerTaskDTO = getById(id);
        if (bookFileUserWorkerTaskDTO.getStatus() == IN_PROGRESS) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_UPDATE_WITH_STATUS.getErrorMessage(id, bookFileUserWorkerTaskDTO.getStatus()));
        }

        bookFileUserWorkerTaskDTO.setStatus(WorkStatus.NOT_STARTED);

        return super.patchById(id, patchDto);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        BookFileUserWorkerTaskDTO bookFileUserWorkerTaskDTO = getById(id);
        if (bookFileUserWorkerTaskDTO.getStatus() == IN_PROGRESS) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_REMOVAL_WITH_STATUS.getErrorMessage(id, bookFileUserWorkerTaskDTO.getStatus()));
        }
        
        super.deleteById(id);
    }

    @Override
    @Transactional
    protected void processTask(BookFileUserWorkerTaskDTO task) {
        HashMap<String, UUID> formatReferenceIds = new HashMap<>();

        generators.forEach(generator -> {
            try {
                UUID formatId = generator.generate(task.getSourceBookReferenceId());

                formatReferenceIds.put(generator.getExtension(), formatId);
            } catch (RuntimeException e) {
                failTask(task, e.getMessage());
            }

        });

        task.setFormatReferenceIds(formatReferenceIds);
        succeedTask(task);
    }
}
