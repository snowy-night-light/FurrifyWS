package ws.furrify.core.specification;

import ws.furrify.core.entity.BaseEntity;

public interface EntitySpecAndOrStep<ENTITY extends BaseEntity> {
    EntitySpecJoinStep<ENTITY> where(String field, EntitySpecExpression<ENTITY> expression);
}