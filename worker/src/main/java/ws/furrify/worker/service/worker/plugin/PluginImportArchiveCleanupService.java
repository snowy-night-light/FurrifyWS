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

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ws.furrify.core.utils.SecurityContextUtils;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;
import ws.furrify.worker.domain.worker.WorkStatus;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTask;
import ws.furrify.worker.domain.worker.plugin.PluginImportUserWorkerTaskRepository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Deletes the uploaded archive of plugin import tasks that ended (completed, failed, cancelled):
 * an ended task never runs again, so nothing needs the file. Tasks keep their record and file
 * name; {@code fileRemoved} marks them. An archive shared with a task that is still waiting or
 * running stays. Runs every 15 minutes for tasks ended at least {@link #GRACE} ago, which also
 * clears archives of tasks that ended before this job existed.
 */
@Slf4j
@Service
public class PluginImportArchiveCleanupService {

    static final Duration GRACE = Duration.ofMinutes(10);
    private static final int BATCH = 100;
    private static final Set<WorkStatus> ENDED = EnumSet.of(WorkStatus.COMPLETED, WorkStatus.FAILED, WorkStatus.CANCELLED);
    private static final Set<WorkStatus> ACTIVE = EnumSet.of(WorkStatus.NOT_STARTED, WorkStatus.IN_PROGRESS);

    private final PluginImportUserWorkerTaskRepository repository;
    private final AttachmentFileV1RestControllerApiClient attachmentClient;
    private final TransactionTemplate tx;

    public PluginImportArchiveCleanupService(PluginImportUserWorkerTaskRepository repository,
                                             AttachmentFileV1RestControllerApiClient attachmentClient,
                                             PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.attachmentClient = attachmentClient;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
    public void removeArchivesOfEndedTasks() {
        int removed = 0, failed = 0;
        Set<UUID> failedThisRun = new HashSet<>();
        try {
            while (true) {
                List<PluginImportUserWorkerTask> tasks = repository.findWithRemovableArchive(ENDED, ACTIVE,
                        ZonedDateTime.now().minus(GRACE), PageRequest.of(0, BATCH));
                tasks.removeIf(t -> failedThisRun.contains(t.getFileReferenceId()));
                if (tasks.isEmpty()) break;
                for (PluginImportUserWorkerTask task : tasks) {
                    UUID fileId = task.getFileReferenceId();
                    if (failedThisRun.contains(fileId)) continue;
                    if (deleteArchive(fileId, task.getOwnerId())) {
                        tx.executeWithoutResult(s -> repository.markFileRemoved(fileId));
                        removed++;
                    } else {
                        failedThisRun.add(fileId);
                        failed++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Plugin import archive cleanup stopped: {}", e.getMessage());
        }
        if (removed > 0 || failed > 0) {
            log.info("Plugin import archive cleanup: {} archives of ended tasks deleted, {} could not be deleted (next run retries)", removed, failed);
        }
    }

    /** True when the archive is gone (deleted now, or already). */
    private boolean deleteArchive(UUID fileId, UUID ownerId) {
        if (ownerId != null) {
            SecurityContextUtils.mockFeignClientSecurityContext(ownerId);
        }
        try {
            attachmentClient.attachmentFileV1RestControllerDelete(fileId);
            return true;
        } catch (Exception e) {
            if (isNotFound(e)) {
                return true;
            }
            log.debug("Could not delete plugin import archive {}: {}", fileId, e.getMessage());
            return false;
        } finally {
            SecurityContextUtils.clearFeignClientSecurityContext();
        }
    }

    private static boolean isNotFound(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof FeignException fe && fe.status() == 404) return true;
            if (t.getCause() == t) break;
        }
        return false;
    }
}
