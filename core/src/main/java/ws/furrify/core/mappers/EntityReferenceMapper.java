package ws.furrify.core.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.TargetType;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.request.EntityIdRequest;

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
}