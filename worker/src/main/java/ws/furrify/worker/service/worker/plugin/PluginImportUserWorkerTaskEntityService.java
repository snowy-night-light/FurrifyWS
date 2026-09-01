package ws.furrify.worker.service.worker.plugin;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AttachmentFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.request.EmptyPatchEntityRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.LibraryV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.shared.plugin.ImportV1WorkerPluginIntf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.List;

import static ws.furrify.core.specification.EntitySpec.specEquals;
import static ws.furrify.core.specification.EntitySpec.specLessThan;
import static ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED;

@Service
@Slf4j
public class PluginImportUserWorkerTaskEntityService extends BaseEntityCrudService<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> {

    private final ExternalPluginLoaderService externalPluginLoaderService;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final LibraryV1RestControllerApiClient libraryV1RestControllerApiClient;
    private final AsyncUtils asyncUtils;

    @Autowired
    public PluginImportUserWorkerTaskEntityService(BaseEntityRepository<PluginImportUserWorkerTask> entityRepository, BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, EmptyPatchEntityRequest<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO>> dtoMapper, ExternalPluginLoaderService externalPluginLoaderService, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, LibraryV1RestControllerApiClient libraryV1RestControllerApiClient, AsyncUtils asyncUtils) {
        super(entityRepository, dtoMapper);
        this.externalPluginLoaderService = externalPluginLoaderService;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.libraryV1RestControllerApiClient = libraryV1RestControllerApiClient;
        this.asyncUtils = asyncUtils;
    }

    @Override
    @Transactional
    public PluginImportUserWorkerTaskDTO create(PluginImportUserWorkerTaskDTO dto) {
        if (!getPluginProviders().contains(dto.getProvider())) {
            throw new ServiceLogicException(Errors.UNRECOGNIZED_PROVIDER.getErrorMessage(dto.getProvider()));
        }

        if (attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(dto.getFileReferenceId()).getBody() == null) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
        }

        if (libraryV1RestControllerApiClient.libraryV1RestControllerGetById(dto.getDestinationLibraryReferenceId()).getBody() == null) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getDestinationLibraryReferenceId()));
        }


        // TODO REMOVE
        var meregedDto = super.create(dto);

        this.asyncUtils.runAsync(this::processImportWorkerTasks);

        return meregedDto;
    }

    private List<String> getPluginProviders() {
        List<ImportV1WorkerPluginIntf> plugins = externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class);

        return plugins.stream().map(ImportV1WorkerPluginIntf::getProviderName).toList();
    }

    //@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES) TODO
    @Transactional
    protected void processImportWorkerTasks() {
        EntitySpecResult<PluginImportUserWorkerTask> spec = EntitySpec.<PluginImportUserWorkerTask>specBuilder()
                .where("status", specEquals(NOT_STARTED))
                .and()
                .where("startAt", specLessThan(ZonedDateTime.now()))
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));

        List<ImportV1WorkerPluginIntf> plugins = externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class);

        Page<PluginImportUserWorkerTaskDTO> tasks = this.getAllPaged(spec.specString(), pageable);

        tasks.forEach(task -> {
            ImportV1WorkerPluginIntf plugin = plugins.stream()
                    .filter(p -> p.getProviderName().equals(task.getProvider()))
                    .findFirst()
                    .orElse(null);

            if (plugin == null) {
                log.error("Plugin [provider={}] not found! Cannot process scheduled task.", task.getProvider());
                failTask(task, "Plugin [provider=" + task.getProvider() + "] not found! Cannot process scheduled task.");
                return;
            }

            AttachmentFileDTO attachmentFileDTO = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(task.getFileReferenceId()).getBody();

            if (attachmentFileDTO == null || attachmentFileDTO.getFileUri() == null) {
                log.error("File reference [id={}] not found! Cannot process scheduled task.", task.getFileReferenceId());
                failTask(task, "File reference [id=" + task.getFileReferenceId() + "] not found! Cannot process scheduled task.");
                return;
            }

            Path tempFilePath;
            try {
                tempFilePath = Files.createTempFile("plugin-iuwt-", "." + attachmentFileDTO.getFileExtension());
            } catch (IOException e) {
                log.error(e.getMessage());
                failTask(task, "Error processing file: " + e.getMessage());
                return;
            }

            File tempFile = tempFilePath.toFile();

            try (InputStream in = attachmentFileDTO.getFileUri().toURL().openStream()) {
                Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);

                if (!plugin.validateSchema(tempFile)) {
                    failTask(task, "File reference [id=" + task.getFileReferenceId() + "] failed pre plugin validation.");
                    return;
                }

                plugin.loadSchemaDataIntoLibrary(tempFile, task.getDestinationLibraryReferenceId());

                task.setStatus(WorkStatus.COMPLETED);
                task.setFinishedAt(ZonedDateTime.now());
                this.internalPutById(task.getId(), task);

            } catch (IOException e) {
                log.error(e.getMessage());
                failTask(task, "Error processing file: " + e.getMessage());
            } finally {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException _) {}
            }
        });
    }

    @Transactional
    protected void failTask(PluginImportUserWorkerTaskDTO task, String errorMessage) {
        task.setStatus(WorkStatus.FAILED);
        task.setErrors(List.of(errorMessage));
        task.setFinishedAt(ZonedDateTime.now());
        this.internalPutById(task.getId(), task);
    }
}
