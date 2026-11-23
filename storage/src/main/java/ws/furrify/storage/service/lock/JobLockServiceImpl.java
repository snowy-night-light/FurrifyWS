/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.service.lock;

import org.springframework.stereotype.Service;
import ws.furrify.core.lock.JobLockService;
import ws.furrify.storage.StorageApplication;
import ws.furrify.storage.domain.lock.JobLockImpl;
import ws.furrify.storage.domain.lock.JobLockImplRepository;

/** Storage's job leases, owned by this instance ({@link StorageApplication#LAUNCH_ID}). */
@Service
public class JobLockServiceImpl extends JobLockService<JobLockImpl> {

    public JobLockServiceImpl(JobLockImplRepository repository) {
        super(repository, StorageApplication.LAUNCH_ID);
    }

    @Override
    protected JobLockImpl newLock(String name) {
        return new JobLockImpl(name);
    }
}
