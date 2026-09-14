package ws.furrify.worker.service.worker.plugin;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AttachmentFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.LibraryV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.dto.worker.plugin.ImportWorkerPluginDTO;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.PatchPluginImportUserWorkerTaskRequest;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;
import ws.furrify.worker.shared.plugin.ImportV1WorkerPluginIntf;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.openapitools.model.FileUploadStatus.UPLOADED;
import static ws.furrify.core.specification.EntitySpec.specEquals;
import static ws.furrify.core.specification.EntitySpec.specLessThan;
import static ws.furrify.worker.domain.worker.WorkStatus.COMPLETED;
import static ws.furrify.worker.domain.worker.WorkStatus.IN_PROGRESS;
import static ws.furrify.worker.domain.worker.WorkStatus.NOT_STARTED;

@Service
@Slf4j
public class PluginImportUserWorkerTaskEntityService extends UserWorkerTaskBaseEntityService<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, PatchPluginImportUserWorkerTaskRequest> {

    private final ExternalPluginLoaderService externalPluginLoaderService;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final LibraryV1RestControllerApiClient libraryV1RestControllerApiClient;

    @Value("${FURRIFY_CDN_URL:}")
    private String cdnUrl;

    @Autowired
    public PluginImportUserWorkerTaskEntityService(BaseEntityRepository<PluginImportUserWorkerTask> entityRepository, BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, PatchPluginImportUserWorkerTaskRequest> dtoMapper, AsyncUtils asyncUtils, ExternalPluginLoaderService externalPluginLoaderService, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, LibraryV1RestControllerApiClient libraryV1RestControllerApiClient) {
        super(entityRepository, dtoMapper, asyncUtils);
        this.externalPluginLoaderService = externalPluginLoaderService;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.libraryV1RestControllerApiClient = libraryV1RestControllerApiClient;
    }

    @Override
    @Transactional
    public PluginImportUserWorkerTaskDTO create(PluginImportUserWorkerTaskDTO dto) {
        List<ImportV1WorkerPluginIntf> plugins = externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class);

        // Find the plugin matching the requested provider class
        ImportV1WorkerPluginIntf selectedPlugin = plugins.stream()
                .filter(p -> p.getClass().getSimpleName().equals(dto.getProvider()))
                .findFirst()
                .orElseThrow(() -> new ServiceLogicException(Errors.UNRECOGNIZED_PROVIDER.getErrorMessage(dto.getProvider())));

        AttachmentFileDTO attachmentFile;
        try {
            attachmentFile = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(dto.getFileReferenceId()).getBody();
            if (attachmentFile == null) {
                throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
        }

        // Validate the attachment file extension against the plugin's allowed extensions
        String[] allowedExtensions = selectedPlugin.getAllowedExtensions();
        if (allowedExtensions != null && allowedExtensions.length > 0) {
            String fileExtension = attachmentFile.getFileExtension();
            boolean extensionAllowed = Arrays.stream(allowedExtensions)
                    .anyMatch(ext -> ext.equalsIgnoreCase(fileExtension));
            if (!extensionAllowed) {
                throw new ServiceLogicException(Errors.EXTENSION_NOT_ALLOWED.getErrorMessage(
                        fileExtension,
                        dto.getProvider(),
                        String.join(", ", allowedExtensions)
                ));
            }
        }

        try {
            if (libraryV1RestControllerApiClient.libraryV1RestControllerGetById(dto.getDestinationLibraryReferenceId()).getBody() == null) {
                throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getDestinationLibraryReferenceId()));
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getDestinationLibraryReferenceId()));
        } catch (Exception e) {
            throw new ServiceLogicException("Failed to verify destination library: " + e.getMessage());
        }

        return super.create(dto);
    }

    @Override
    @Transactional
    public PluginImportUserWorkerTaskDTO patchById(UUID id, PatchPluginImportUserWorkerTaskRequest patchDto) {
        PluginImportUserWorkerTaskDTO pluginImportUserWorkerTaskDTO = getById(id);
        if (pluginImportUserWorkerTaskDTO.getStatus() == IN_PROGRESS || pluginImportUserWorkerTaskDTO.getStatus() == COMPLETED) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_UPDATE_WITH_STATUS.getErrorMessage(id, pluginImportUserWorkerTaskDTO.getStatus().name()));
        }

        return super.patchById(id, patchDto);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void processImportWorkerTasks() {
        EntitySpecResult<PluginImportUserWorkerTask> spec = EntitySpec.<PluginImportUserWorkerTask>specBuilder()
                .where("status", specEquals(NOT_STARTED))
                .and()
                .where("startAt", specLessThan(ZonedDateTime.now()))
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<PluginImportUserWorkerTaskDTO> tasks = this.getAllPaged(spec.specString(), pageable);
        tasks.forEach(this::triggerExecution);
    }

    @Transactional
    protected void processTask(PluginImportUserWorkerTaskDTO task) {
        SecurityContextUtils.mockFeignClientSecurityContext(task.getOwnerId());

        try {
            List<ImportV1WorkerPluginIntf> plugins = externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class);

            ImportV1WorkerPluginIntf plugin = plugins.stream()
                    .filter(p -> p.getClass().getSimpleName().equals(task.getProvider()))
                    .findFirst()
                    .orElse(null);

            if (plugin == null) {
                log.error("Plugin [provider={}] not found! Cannot process scheduled task.", task.getProvider());
                failTask(task, "Plugin [provider=" + task.getProvider() + "] not found! Cannot process scheduled task.");
                return;
            }

            AttachmentFileDTO attachmentFileDTO = null;
            try {
                attachmentFileDTO = attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerGetById(task.getFileReferenceId()).getBody();
            } catch (Exception e) {
                log.error("Failed to fetch attachment file reference [id={}]: {}", task.getFileReferenceId(), e.getMessage());
                failTask(task, "Failed to fetch attachment file reference: " + e.getMessage());
                return;
            }

            if (attachmentFileDTO == null || attachmentFileDTO.getFileUri() == null || !UPLOADED.name().equals(attachmentFileDTO.getUploadStatus().name())) {
                log.error("File reference [id={}] not found or not uploaded! Cannot process scheduled task.", task.getFileReferenceId());
                failTask(task, "File reference [id=" + task.getFileReferenceId() + "] not found or not uploaded! Cannot process scheduled task.");
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

            URI fileUri = attachmentFileDTO.getFileUri();
            String urlString = fileUri.toString();
            if (cdnUrl != null && !cdnUrl.isBlank()) {
                urlString = cdnUrl + (urlString.startsWith("/") ? "" : "/") + urlString;
            }
            if (Thread.currentThread().isInterrupted()) {
                log.warn("Task thread interrupted for task {}", task.getId());
                return;
            }

            try (InputStream in = URI.create(urlString).toURL().openStream()) {
                Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);

                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task thread interrupted for task {}", task.getId());
                    return;
                }

                if (!plugin.validateSchema(tempFile)) {
                    failTask(task, "File reference [id=" + task.getFileReferenceId() + "] failed pre plugin validation.");
                    return;
                }

                var results = plugin.loadSchemaDataIntoLibrary(tempFile, task.getDestinationLibraryReferenceId());

                Optional<PluginImportUserWorkerTaskDTO> optionalTask = this.findById(task.getId());
                if (optionalTask.isEmpty()) {
                    return;
                }
                PluginImportUserWorkerTaskDTO latestTask = optionalTask.get();
                if (results != null) {
                    latestTask.setErrors(results.getErrors());
                    latestTask.setWarnings(results.getWarnings());
                    latestTask.setLog(results.getLog());
                }
                succeedTask(latestTask);

                asyncUtils.runAsyncAfterCommit(() -> {
                    try {
                        attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(latestTask.getFileReferenceId());
                    } catch (Exception e) {
                        log.error("Failed to delete attachment file after task completion: {}", e.getMessage());
                    }
                });

            } catch (Exception e) {
                Throwable cause = e.getCause();
                while (cause != null && !(cause instanceof InterruptedException)) {
                    cause = cause.getCause();
                }
                
                if (Thread.currentThread().isInterrupted() || cause != null) {
                    log.warn("Task thread interrupted during file processing for task {}", task.getId());
                } else {
                    log.error("Plugin execution failed: {}", e.getMessage(), e);
                    failTask(task, "Error processing file: " + e.getMessage());
                }
            } finally {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException _) {
                }
            }
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }


    public List<ImportWorkerPluginDTO> getAllPlugins() {
        return externalPluginLoaderService.getPlugins(ImportV1WorkerPluginIntf.class).stream()
                .map(plugin -> ImportWorkerPluginDTO.builder()
                        .provider(plugin.getClass().getSimpleName())
                        .name(plugin.getProviderName())
                        .allowedExtensions(plugin.getAllowedExtensions())
                        .build())
                .collect(Collectors.toUnmodifiableList());
    }
}
