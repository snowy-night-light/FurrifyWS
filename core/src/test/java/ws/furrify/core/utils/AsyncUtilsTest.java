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
        asyncUtils.shutdown();
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
    void runAsyncLater_runsAfterTheDelayInTransaction() throws Exception {
        java.util.concurrent.CountDownLatch ran = new java.util.concurrent.CountDownLatch(1);
        long start = System.nanoTime();
        asyncUtils.runAsyncLater(ran::countDown, java.time.Duration.ofMillis(200));

        assertTrue(ran.await(5, java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(System.nanoTime() - start >= 200_000_000L, "not before the delay");
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    void runAsyncLater_cancelledBeforeTheDelay_neverRuns() throws Exception {
        AtomicBoolean ran = new AtomicBoolean(false);
        var future = asyncUtils.runAsyncLaterNoTransaction(() -> ran.set(true), java.time.Duration.ofMillis(300));
        future.cancel(false);

        Thread.sleep(600);
        assertFalse(ran.get());
        assertEquals(0, transactionManager.getTransactionCount());
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
        assertEquals(1, transactionManager.getSuspendCount(), "finished transaction suspended before the task's own");
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
        assertEquals(1, transactionManager.getSuspendCount(), "caller-runs task must not join the finished transaction");
    }

    @Test
    void runAsyncAfterCommitNoTransaction_whenSynchronizationActive_suspendsFinishedTransaction() {
        TransactionSynchronizationManager.initSynchronization();

        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAsyncAfterCommitNoTransaction(() -> ran.set(true));

        assertFalse(ran.get());

        TransactionSynchronizationUtils.triggerAfterCommit();

        assertTrue(ran.get());
        assertEquals(0, transactionManager.getTransactionCount());
        assertEquals(1, transactionManager.getSuspendCount());
    }

    @Test
    void runAfterCommitNoTransaction_whenSynchronizationActive_runsInlineAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();

        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAfterCommitNoTransaction(() -> ran.set(true));

        assertFalse(ran.get());

        TransactionSynchronizationUtils.triggerAfterCommit();

        assertTrue(ran.get());
        assertEquals(0, transactionManager.getTransactionCount());
    }

    @Test
    void runAfterCommitNoTransaction_whenNoSynchronization_runsImmediately() {
        AtomicBoolean ran = new AtomicBoolean(false);
        asyncUtils.runAfterCommitNoTransaction(() -> ran.set(true));

        assertTrue(ran.get());
        assertEquals(0, transactionManager.getTransactionCount());
        assertEquals(0, transactionManager.getSuspendCount());
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
        private final AtomicInteger suspendCount = new AtomicInteger(0);

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            if (definition != null && definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
                suspendCount.incrementAndGet();
                return new SimpleTransactionStatus(false);
            }
            transactionCount.incrementAndGet();
            return new SimpleTransactionStatus(true);
        }

        public int getSuspendCount() {
            return suspendCount.get();
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
