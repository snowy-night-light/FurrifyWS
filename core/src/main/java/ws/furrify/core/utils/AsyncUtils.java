/*
 * furrify-core - Furrify Workspace Project
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
package ws.furrify.core.utils;

import org.hibernate.StaleStateException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class AsyncUtils {

    private final TransactionTemplate transactionTemplate;
    private final TaskExecutor taskExecutor;

    public AsyncUtils(PlatformTransactionManager transactionManager, TaskExecutor taskExecutor) {
        if (transactionManager != null) {
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else {
            this.transactionTemplate = null;
        }
        this.taskExecutor = taskExecutor != null ? taskExecutor : new SimpleAsyncTaskExecutor();
    }

    private Runnable withContext(Runnable task) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        return () -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                if (requestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                }
                task.run();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }

    public void runInTransaction(Runnable task) {
        executeInTransaction(task);
    }

    public void runAsync(Runnable task) {
        taskExecutor.execute(withContext(() -> executeInTransaction(task)));
    }

    public void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeInTransaction(task);
                }
            });
        } else {
            executeInTransaction(task);
        }
    }

    public void runAsyncNoTransaction(Runnable task) {
        taskExecutor.execute(withContext(task));
    }

    public void runAsyncAfterCommitNoTransaction(Runnable task) {
        Runnable contextAwareTask = withContext(task);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    taskExecutor.execute(contextAwareTask);
                }
            });
        } else {
            taskExecutor.execute(contextAwareTask);
        }
    }

    public void runAsyncAfterCommit(Runnable task) {
        Runnable contextAwareTask = withContext(() -> executeInTransaction(task));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    taskExecutor.execute(contextAwareTask);
                }
            });
        } else {
            taskExecutor.execute(contextAwareTask);
        }
    }

    private void executeInTransaction(Runnable task) {
        if (transactionTemplate != null) {
            int maxRetries = 15;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    transactionTemplate.executeWithoutResult(status -> task.run());
                    return;
                } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
                    if (i == maxRetries - 1) {
                        throw e;
                    }
                    try {
                        long baseSleep = 50L * (i + 1);
                        long jitter = (long) (Math.random() * 50);
                        Thread.sleep(baseSleep + jitter);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        } else {
            task.run();
        }
    }
}