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
