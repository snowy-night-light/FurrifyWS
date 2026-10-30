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
package ws.furrify.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class UserScopedEntity extends BaseEntity {
    @Column(nullable = false)
    UUID ownerId;

    @Override
    protected void onCreate() {
        super.onCreate();

        if (this.ownerId == null) {
            this.ownerId = SecurityContextUtils.getCurrentSubject().orElseThrow(() -> new RuntimeException("User context was not found when attempting to create user scoped entity."));
        }
    }

}
