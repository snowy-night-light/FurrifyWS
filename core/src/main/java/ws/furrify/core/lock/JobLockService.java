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
package ws.furrify.core.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Cluster-wide leases for jobs only one instance of a service may run at a time, kept in the service's own
 * database through Hibernate (no database-specific locking). A lease runs out by itself, so a crashed
 * instance can't hold a job forever. A service extends this as a bean for its {@link JobLock} entity:
 * <pre>{@code
 * @Service
 * class JobLockServiceImpl extends JobLockService<JobLockImpl> {
 *     JobLockServiceImpl(JobLockImplRepository repository) { super(repository, StorageApplication.LAUNCH_ID); }
 *     protected JobLockImpl newLock(String name) { return new JobLockImpl(name); }
 * }
 * }</pre>
 *
 * @param <LOCK> the service's lock entity
 */
@Slf4j
public abstract class JobLockService<LOCK extends JobLock> {

    private final JobLockRepository<LOCK> repository;
    /** This instance: the owner recorded with the leases it holds. */
    private final UUID owner;

    protected JobLockService(JobLockRepository<LOCK> repository, UUID owner) {
        this.repository = repository;
        this.owner = owner;
    }

    /** A new, free lock row for {@code name}. */
    protected abstract LOCK newLock(String name);

    /**
     * Take the job's lease for {@code duration}; false while another instance holds it. Every step commits on
     * its own (repository transactions): a row created by another instance at the same moment only fails that insert.
     */
    public boolean tryAcquire(String job, Duration duration) {
        if (!repository.existsById(job)) {
            try {
                repository.saveAndFlush(newLock(job));
            } catch (DataIntegrityViolationException createdMeanwhile) {
                // another instance created the row at the same time: fine, it exists now
            }
        }
        ZonedDateTime now = ZonedDateTime.now();
        return repository.acquire(job, owner, now, now.plus(duration)) > 0;
    }

    /** Give the lease back (only if this instance holds it). */
    public void release(String job) {
        repository.release(job, owner);
    }

    /**
     * Run {@code task} if this instance gets the job's lease, and release it afterwards. False when another
     * instance holds the lease (or it couldn't be taken); {@code duration} should outlast a run, as the lease
     * isn't refreshed while the task runs.
     */
    public boolean runExclusively(String job, Duration duration, Runnable task) {
        try {
            if (!tryAcquire(job, duration)) return false;
        } catch (RuntimeException e) {
            log.warn("Job {} skipped: its lock could not be taken: {}", job, e.getMessage());
            return false;
        }
        try {
            task.run();
            return true;
        } finally {
            try {
                release(job);
            } catch (RuntimeException e) {
                log.debug("Lock of job {} not released (it runs out by itself): {}", job, e.getMessage());
            }
        }
    }
}
