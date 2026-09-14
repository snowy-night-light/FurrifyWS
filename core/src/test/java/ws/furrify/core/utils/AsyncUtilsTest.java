package ws.furrify.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncUtilsTest {

    private TestTransactionManager transactionManager;
    private AsyncUtils asyncUtils;

    @BeforeEach
    void setUp() {
        transactionManager = new TestTransactionManager();
        asyncUtils = new AsyncUtils(transactionManager, new SyncTaskExecutor());
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void runAsync_runsInTransaction() {
        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAsync(() -> ran.set(true));

        assertTrue(ran.get());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAfterCommit_whenSynchronizationActive_runsAfterCommitInNewTransaction() {
        TransactionSynchronizationManager.initSynchronization();

        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAfterCommit(() -> ran.set(true));

        assertFalse(ran.get());

        TransactionSynchronizationUtils.triggerAfterCommit();

        assertTrue(ran.get());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAfterCommit_whenNoSynchronization_runsImmediatelyInTransaction() {
        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAfterCommit(() -> ran.set(true));

        assertTrue(ran.get());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAsyncAfterCommit_whenSynchronizationActive_delegatesToAsyncAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();

        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAsyncAfterCommit(() -> ran.set(true));

        assertFalse(ran.get());

        TransactionSynchronizationUtils.triggerAfterCommit();

        assertTrue(ran.get());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAsyncAfterCommit_whenNoSynchronization_delegatesToAsyncImmediately() {
        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAsyncAfterCommit(() -> ran.set(true));

        assertTrue(ran.get());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAfterCommit_whenTransactionRolledBack_doesNotRun() {
        TransactionSynchronizationManager.initSynchronization();

        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAfterCommit(() -> ran.set(true));

        TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertFalse(ran.get());
        assertEquals(0, transactionManager.getTransactionCount());
    }

    private static class TestTransactionManager implements PlatformTransactionManager {
        private final AtomicInteger transactionCount = new AtomicInteger(0);

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            transactionCount.incrementAndGet();
            return new SimpleTransactionStatus(true);
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {
        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
        }

        public int getTransactionCount() {
            return transactionCount.get();
        }
    }
}
