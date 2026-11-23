/*
 * furrify-worker-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.worker.service.worker.plugin;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AttachmentFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.core.utils.AsyncUtils;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.openapi.gen.storage.api.LibraryV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTaskRepository;
import ws.furrify.worker.dto.worker.plugin.ImportWorkerPluginDTO;
import ws.furrify.worker.dto.worker.plugin.PluginImportUserWorkerTaskDTO;
import ws.furrify.worker.dto.worker.plugin.request.PatchPluginImportUserWorkerTaskRequest;
import ws.furrify.worker.model.WorkerPluginResults;
import ws.furrify.worker.service.worker.AttachmentService;
import ws.furrify.worker.service.worker.UserWorkerTaskBaseEntityService;
import ws.furrify.worker.shared.plugin.ImportV1WorkerPluginIntf;
import ws.furrify.worker.shared.plugin.exception.WorkerErrors;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import feign.FeignException;
import java.time.Duration;

import static ws.furrify.worker.domain.worker.WorkStatus.COMPLETED;
import static ws.furrify.worker.domain.worker.WorkStatus.IN_PROGRESS;

@Service
@Slf4j
public class PluginImportUserWorkerTaskEntityService extends UserWorkerTaskBaseEntityService<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, PatchPluginImportUserWorkerTaskRequest> {

    private final ExternalPluginLoaderService externalPluginLoaderService;
    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;
    private final LibraryV1RestControllerApiClient libraryV1RestControllerApiClient;
    private final AttachmentService attachmentService;
    private final ConcurrentHashMap<UUID, ImportV1WorkerPluginIntf> activePlugins = new ConcurrentHashMap<>();
    /** destination library id -> the import task currently writing into it (this worker instance). */
    private final ConcurrentHashMap<UUID, UUID> librariesInImport = new ConcurrentHashMap<>();
    private final PluginImportUserWorkerTaskRepository pluginImportUserWorkerTaskRepository;

    @Value("${FURRIFY_CDN_URL:}")
    private String cdnUrl;

    @Autowired
    public PluginImportUserWorkerTaskEntityService(BaseEntityRepository<PluginImportUserWorkerTask> entityRepository, BaseDTOMapper<PluginImportUserWorkerTask, PluginImportUserWorkerTaskDTO, PatchPluginImportUserWorkerTaskRequest> dtoMapper, AsyncUtils asyncUtils, ExternalPluginLoaderService externalPluginLoaderService, AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient, LibraryV1RestControllerApiClient libraryV1RestControllerApiClient, AttachmentService attachmentService, PluginImportUserWorkerTaskRepository pluginImportUserWorkerTaskRepository) {
        super(entityRepository, dtoMapper, asyncUtils);
        this.pluginImportUserWorkerTaskRepository = pluginImportUserWorkerTaskRepository;
        this.externalPluginLoaderService = externalPluginLoaderService;
        this.attachmentFileV1RestControllerApiClient = attachmentFileV1RestControllerApiClient;
        this.libraryV1RestControllerApiClient = libraryV1RestControllerApiClient;
        this.attachmentService = attachmentService;
    }

    @Override
    @Transactional
    protected PluginImportUserWorkerTaskDTO handleCreate(PluginImportUserWorkerTaskDTO dto) {
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
        } catch (FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getFileReferenceId()));
        }

        // Remember the original file name for task details: the attachment itself is deleted after the import.
        dto.setFileName(attachmentFile.getFileName());

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
        } catch (FeignException.NotFound e) {
            throw new ReferenceNotFoundException(Errors.REFERENCE_NOT_FOUND.getErrorMessage(dto.getDestinationLibraryReferenceId()));
        } catch (Exception e) {
            throw new ServiceLogicException("Failed to verify destination library: " + e.getMessage());
        }

        return super.handleCreate(dto);
    }

    @Override
    @Transactional
    protected PluginImportUserWorkerTaskDTO handlePatch(UUID id, PatchPluginImportUserWorkerTaskRequest patchDto) {
        PluginImportUserWorkerTaskDTO pluginImportUserWorkerTaskDTO = getById(id);
        if (pluginImportUserWorkerTaskDTO.getStatus() == IN_PROGRESS || pluginImportUserWorkerTaskDTO.getStatus() == COMPLETED) {
            throw new ServiceLogicException(WorkerErrors.TASK_DOESNT_ALLOW_UPDATE_WITH_STATUS.getErrorMessage(id, pluginImportUserWorkerTaskDTO.getStatus().name()));
        }

        return super.handlePatch(id, patchDto);
    }

    @Transactional
    @Override
    protected void processTask(PluginImportUserWorkerTaskDTO task) {
        // One import per library at a time. Concurrent imports into the same library race on the
        // same books (each only deduplicates its own threads): both see chapters missing and both
        // create them ("Chapter with number N already exists"), and books/artists, which have no
        // unique (library, externalId) constraint, can be duplicated. A waiting import is put back
        // in the queue and starts on a later poll, once the running one has finished.
        UUID libraryId = task.getDestinationLibraryReferenceId();
        if (!claimLibrary(libraryId, task)) {
            rescheduleTask(task, "Another import into library " + libraryId + " is running; this one waits until it finishes.",
                    Duration.ofSeconds(30));
            return;
        }

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

            File tempFile = attachmentService.downloadAttachment(task.getFileReferenceId(), task.getOwnerId(), "plugin-iuwt-");

            if (tempFile == null) {
                failTask(task, "Failed to fetch attachment file reference [id=" + task.getFileReferenceId() + "] or file not uploaded.");
                return;
            }

            try {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task thread interrupted for task {}", task.getId());
                    return;
                }

                if (!plugin.validateSchema(tempFile)) {
                    failTask(task, "File reference [id=" + task.getFileReferenceId() + "] failed pre plugin validation.");
                    return;
                }

                WorkerPluginResults results;
                activePlugins.put(task.getId(), plugin);
                try {
                    results = plugin.loadSchemaDataIntoLibrary(tempFile, task.getDestinationLibraryReferenceId(), task.getDownloadExternalMedia());
                } finally {
                    activePlugins.remove(task.getId());
                }

                if (!saveResults(task.getId(), results)) {
                    return; // task was deleted while the import ran
                }
                succeedTask(task);

                UUID fileReferenceId = task.getFileReferenceId();
                asyncUtils.runAsyncAfterCommit(() -> {
                    try {
                        attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(fileReferenceId);
                    } catch (Exception e) {
                        log.error("Failed to delete attachment file after task completion: {}", e.getMessage());
                    }
                });

            } catch (Exception e) {
                saveResults(task.getId(), plugin.trackCurrentStatus());

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
                attachmentService.cleanupTmpFile(tempFile);
            }
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
            librariesInImport.remove(libraryId, task.getId());
        }
    }

    /**
     * Claim {@code libraryId} for {@code task}: free in this instance and no import into it started
     * earlier elsewhere (still IN_PROGRESS). On false nothing is held.
     */
    private boolean claimLibrary(UUID libraryId, PluginImportUserWorkerTaskDTO task) {
        if (libraryId == null) {
            return true;
        }
        UUID holder = librariesInImport.putIfAbsent(libraryId, task.getId());
        if (holder != null && !holder.equals(task.getId())) {
            return false;
        }
        boolean earlierElsewhere;
        try {
            // Plain read: needs no transaction (unlike the locking reads elsewhere in this class).
            earlierElsewhere = task.getStartedAt() != null
                    && pluginImportUserWorkerTaskRepository.existsEarlierImportIntoLibrary(libraryId, IN_PROGRESS, task.getId(), task.getStartedAt());
        } catch (RuntimeException e) {
            librariesInImport.remove(libraryId, task.getId());
            throw e;
        }
        if (earlierElsewhere) {
            librariesInImport.remove(libraryId, task.getId());
            return false;
        }
        return true;
    }

    /**
     * Persist the plugin's final log/warnings/errors on the task, in its own transaction.
     * <p>
     * processTask runs on a pool thread outside any transaction, and {@code this.internalFindById}
     * is a self-invocation (no transactional proxy), so the locking read it does
     * ({@code SELECT ... FOR UPDATE}) failed there with "No active transaction". Also,
     * succeedTask/failTask re-read the task, so results set on a separately loaded copy were
     * silently dropped; saving them first means those re-reads pick them up.
     *
     * @return false when the task no longer exists
     */
    private boolean saveResults(UUID taskId, WorkerPluginResults results) {
        boolean[] exists = {false};
        asyncUtils.runInTransaction(() -> this.internalFindById(taskId).ifPresent(latestTask -> {
            exists[0] = true;
            if (results != null) {
                latestTask.setErrors(results.getErrors());
                latestTask.setWarnings(results.getWarnings());
                latestTask.setLog(results.getLog());
                truncateTaskData(latestTask);
                this.internalPutById(taskId, latestTask);
            }
        }));
        return exists[0];
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void trackActivePluginStatuses() {
        activePlugins.forEach((taskId, plugin) -> {
            try {
                var results = plugin.trackCurrentStatus();
                if (results != null) {
                    PluginImportUserWorkerTaskDTO latestTask = this.internalFindById(taskId).orElse(null);
                    if (latestTask != null && latestTask.getStatus() == IN_PROGRESS) {
                        latestTask.setErrors(results.getErrors());
                        latestTask.setWarnings(results.getWarnings());
                        latestTask.setLog(results.getLog());
                        truncateTaskData(latestTask);
                        this.internalPutById(taskId, latestTask);
                    }
                }
            } catch (OptimisticLockingFailureException e) {
                log.debug("Optimistic lock failure while tracking status for task {}. Ignoring.", taskId);
            } catch (Exception e) {
                log.error("Failed to track status for task {}: {}", taskId, e.getMessage());
            }
        });
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

    @Override
    protected long getTaskTimeoutSeconds() {
        return 24 * 60 * 60 * 30; // 30 days
    }

    @Override
    protected void onTaskCancelledOnShutdown(PluginImportUserWorkerTaskDTO task) {
        ImportV1WorkerPluginIntf plugin = activePlugins.get(task.getId());
        if (plugin != null) {
            try {
                WorkerPluginResults results = plugin.trackCurrentStatus();
                if (results != null) {
                    if (results.getErrors() != null && !results.getErrors().isEmpty()) {
                        if (task.getErrors() == null) {
                            task.setErrors(new ArrayList<>(results.getErrors()));
                        } else {
                            task.getErrors().addAll(results.getErrors());
                        }
                    }
                    if (results.getWarnings() != null && !results.getWarnings().isEmpty()) {
                        task.setWarnings(results.getWarnings());
                    }
                    if (results.getLog() != null) {
                        task.setLog(results.getLog());
                    }
                    truncateTaskData(task);
                }
            } catch (Exception e) {
                log.error("Failed to track status for task {} on shutdown: {}", task.getId(), e.getMessage());
            }
        }
    }
}
