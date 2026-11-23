/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    /**
     * Tasks run after commit would otherwise inherit the thread's already closed transaction and fail on
     * every write; this sets it aside so the task gets a fresh transaction of its own.
     */
    private final TransactionTemplate suspendingTemplate;
    private final TaskExecutor taskExecutor;
    /** Only waits out delays and hands the task to {@link #taskExecutor}; no task runs on it. */
    private final java.util.concurrent.ScheduledExecutorService delayer =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().name("furrify-delay").factory());

    public AsyncUtils(PlatformTransactionManager transactionManager, TaskExecutor taskExecutor) {
        if (transactionManager != null) {
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            this.suspendingTemplate = new TransactionTemplate(transactionManager);
            this.suspendingTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        } else {
            this.transactionTemplate = null;
            this.suspendingTemplate = null;
        }
        this.taskExecutor = taskExecutor != null ? taskExecutor : new SimpleAsyncTaskExecutor();
    }

    private Runnable withContext(Runnable task) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        return () -> {
            // Restore (not clear) what the executing thread had: with CallerRunsPolicy the task can
            // run on the submitting request thread, whose context must survive the task.
            SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
            RequestAttributes previousRequestAttributes = RequestContextHolder.getRequestAttributes();
            try {
                SecurityContextHolder.setContext(securityContext);
                if (requestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                }
                task.run();
            } finally {
                SecurityContextHolder.setContext(previousSecurityContext);
                if (previousRequestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(previousRequestAttributes);
                } else {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
        };
    }

    /**
     * Wrap a task submitted from {@code afterCommit()} so it never joins the finished transaction.
     * <p>
     * During {@code afterCommit()} the transaction is committed but its resources are still bound to
     * the thread, so a {@code @Transactional(REQUIRED)} call there joins it and every UPDATE/DELETE
     * fails with "No active transaction for update or delete query". On a pool thread this is a
     * no-op; it only matters when the executor is saturated and {@code CallerRunsPolicy} runs the
     * task inline on the committing thread. Spring's documented remedy is to run such work in a
     * separate transaction scope; suspending the old one lets inner {@code @Transactional} methods
     * start a real transaction of their own.
     */
    private Runnable detachedFromCompletedTransaction(Runnable task) {
        return () -> {
            if (suspendingTemplate != null && TransactionSynchronizationManager.isSynchronizationActive()) {
                suspendingTemplate.executeWithoutResult(status -> task.run());
            } else {
                task.run();
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
                    detachedFromCompletedTransaction(() -> executeInTransaction(task)).run();
                }
            });
        } else {
            executeInTransaction(task);
        }
    }

    /**
     * Run a cheap, non-transactional task on the committing thread right after commit (immediately
     * when there is no transaction). For hand-offs such as scheduling a debounced job: unlike
     * {@link #runAsyncAfterCommitNoTransaction} it doesn't take a slot in the shared executor queue.
     */
    public void runAfterCommitNoTransaction(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    detachedFromCompletedTransaction(task).run();
                }
            });
        } else {
            task.run();
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
                    taskExecutor.execute(detachedFromCompletedTransaction(contextAwareTask));
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
                    taskExecutor.execute(detachedFromCompletedTransaction(contextAwareTask));
                }
            });
        } else {
            taskExecutor.execute(contextAwareTask);
        }
    }

    /**
     * Run {@code task} in its own transaction on the shared executor after {@code delay}, with the caller's
     * security context. Cancelling the returned future before then drops it (e.g. to debounce).
     */
    public java.util.concurrent.ScheduledFuture<?> runAsyncLater(Runnable task, java.time.Duration delay) {
        return later(withContext(() -> executeInTransaction(task)), delay);
    }

    /** {@link #runAsyncLater} without a transaction around the task. */
    public java.util.concurrent.ScheduledFuture<?> runAsyncLaterNoTransaction(Runnable task, java.time.Duration delay) {
        return later(withContext(task), delay);
    }

    private java.util.concurrent.ScheduledFuture<?> later(Runnable contextAwareTask, java.time.Duration delay) {
        return delayer.schedule(() -> taskExecutor.execute(contextAwareTask), delay.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** Pending delayed tasks are dropped at shutdown (called by Spring as the bean's destroy method). */
    public void shutdown() {
        delayer.shutdownNow();
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