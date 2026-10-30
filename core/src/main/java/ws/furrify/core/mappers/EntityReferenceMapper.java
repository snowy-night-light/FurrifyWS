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
package ws.furrify.core.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.TargetType;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.request.EntityIdRequest;

import java.util.List;
import java.util.UUID;

@Component
public class EntityReferenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public <T extends BaseEntity> T mapFromNullable(JsonNullable<EntityIdRequest> nullable, @TargetType Class<T> type) {
        if (nullable == null || !nullable.isPresent() || nullable.get() == null || nullable.get().getId() == null) {
            return null;
        }

        T proxy = entityManager.getReference(type, nullable.get().getId());
        proxy.setId(nullable.get().getId());
        return proxy;
    }

    public <T extends BaseEntity> T mapFromRequest(EntityIdRequest request, @TargetType Class<T> type) {
        if (request == null || request.getId() == null) {
            return null;
        }

        T proxy = entityManager.getReference(type, request.getId());
        proxy.setId(request.getId());
        return proxy;
    }

    public UUID mapEntityIdRequestToUuid(EntityIdRequest request) {
        return request == null ? null : request.getId();
    }

    public List<UUID> mapJsonNullableIdListToUuidList(JsonNullable<List<EntityIdRequest>> value) {
        if (value == null || !value.isPresent() || value.get() == null) {
            return null;
        }
        return value.get().stream().map(this::mapEntityIdRequestToUuid).toList();
    }

    public List<UUID> mapEntityIdRequestListToUuidList(List<EntityIdRequest> value) {
        if (value == null) {
            return null;
        }
        return value.stream().map(this::mapEntityIdRequestToUuid).toList();
    }
}