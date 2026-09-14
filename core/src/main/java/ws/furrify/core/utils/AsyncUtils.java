package ws.furrify.core.utils;

import org.hibernate.StaleStateException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

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

    public void runAsync(Runnable task) {
        taskExecutor.execute(() -> executeInTransaction(task));
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

    public void runAsyncAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runAsync(task);
                }
            });
        } else {
            runAsync(task);
        }
    }

    private void executeInTransaction(Runnable task) {
        if (transactionTemplate != null) {
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    transactionTemplate.executeWithoutResult(status -> task.run());
                    return;
                } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
                    if (i == maxRetries - 1) {
                        throw e;
                    }
                    try {
                        Thread.sleep(50 * (i + 1));
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
