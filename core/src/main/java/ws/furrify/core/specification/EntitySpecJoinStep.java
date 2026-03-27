package ws.furrify.core.specification;

import ws.furrify.core.entity.BaseEntity;

public interface EntitySpecJoinStep<ENTITY extends BaseEntity> {
    EntitySpecAndOrStep<ENTITY> and();
    EntitySpecAndOrStep<ENTITY> or();
    EntitySpecJoinStep<ENTITY> and(EntitySpecExpression<ENTITY> nested);
    EntitySpecJoinStep<ENTITY> and(EntitySpecResult<ENTITY> parsedResult);
    EntitySpecJoinStep<ENTITY> or(EntitySpecExpression<ENTITY> nested);
    EntitySpecJoinStep<ENTITY> or(EntitySpecResult<ENTITY> parsedResult);
    EntitySpecResult<ENTITY> build();
}