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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * A lease for a job only one instance of a service may run at a time: held by {@link #owner} until
 * {@link #lockedUntil}, one row per job. A service makes it an entity of its own database by extending it:
 * <pre>{@code
 * @Entity(name = "JobLock")
 * @Table(name = "job_lock")
 * public class JobLockImpl extends JobLock { ... }
 * }</pre>
 * with a {@link JobLockRepository} and a {@link JobLockService} for it.
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class JobLock {

    @Id
    @Column(length = 64)
    private String name;

    private ZonedDateTime lockedUntil;

    private UUID owner;

    protected JobLock(String name) {
        this.name = name;
    }
}
