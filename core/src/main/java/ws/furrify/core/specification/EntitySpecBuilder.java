package ws.furrify.core.specification;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

import static ws.furrify.core.specification.EntitySpec.*;

class EntitySpecBuilder<ENTITY extends BaseEntity> implements EntitySpecWhereStep<ENTITY>, EntitySpecJoinStep<ENTITY>, EntitySpecAndOrStep<ENTITY> {

    private final StringBuilder queryStr = new StringBuilder();
    private Specification<ENTITY> combinedSpec = null;
    private boolean useOrLogic = false;

    EntitySpecBuilder() {}

    void initializeFrom(EntitySpecResult<ENTITY> result) {
        if (result != null && result.specString() != null && !result.specString().isEmpty()) {
            this.queryStr.append(result.specString());
            this.combinedSpec = result.specification();
        }
    }

    @Override
    public EntitySpecJoinStep<ENTITY> where(String field, EntitySpecExpression<ENTITY> expr) {
        appendLogicSeparator();
        queryStr.append(JOIN_OPEN_STRING).append(field).append(expr.expression()).append(JOIN_CLOSE_STRING);
        updateSpec(expr.toSpecification(field));
        return this;
    }

    @Override
    public EntitySpecAndOrStep<ENTITY> or() {
        if (!queryStr.isEmpty() && !queryStr.toString().endsWith(OR_EXPRESSION_STRING)) {
            queryStr.append(OR_EXPRESSION_STRING);
        }
        useOrLogic = true;
        return this;
    }

    @Override
    public EntitySpecJoinStep<ENTITY> or(EntitySpecExpression<ENTITY> nested) {
        or();
        queryStr.append(nested.expression());
        updateSpec(nested.toSpecification(null));
        return this;
    }

    @Override
    public EntitySpecJoinStep<ENTITY> or(EntitySpecResult<ENTITY> result) {
        if (result == null || result.specString().isEmpty()) return this;
        or();
        queryStr.append(JOIN_OPEN_STRING).append(result.specString()).append(JOIN_CLOSE_STRING);
        updateSpec(result.specification());
        return this;
    }

    @Override
    public EntitySpecAndOrStep<ENTITY> and() {
        if (!queryStr.isEmpty() && !queryStr.toString().endsWith(AND_EXPRESSION_STRING)) {
            queryStr.append(AND_EXPRESSION_STRING);
        }
        useOrLogic = false;
        return this;
    }

    @Override
    public EntitySpecJoinStep<ENTITY> and(EntitySpecExpression<ENTITY> nested) {
        and();
        queryStr.append(nested.expression());
        updateSpec(nested.toSpecification(null));
        return this;
    }

    @Override
    public EntitySpecJoinStep<ENTITY> and(EntitySpecResult<ENTITY> result) {
        if (result == null || result.specString().isEmpty()) return this;
        and();
        queryStr.append(JOIN_OPEN_STRING).append(result.specString()).append(JOIN_CLOSE_STRING);
        updateSpec(result.specification());
        return this;
    }

    private void appendLogicSeparator() {
        if (!queryStr.isEmpty() && !queryStr.toString().endsWith(AND_EXPRESSION_STRING) && !queryStr.toString().endsWith(OR_EXPRESSION_STRING)) {
            queryStr.append(AND_EXPRESSION_STRING);
        }
    }

    private void updateSpec(Specification<ENTITY> newSpec) {
        if (combinedSpec == null) {
            combinedSpec = Specification.where(newSpec);
        } else {
            combinedSpec = useOrLogic ? combinedSpec.or(newSpec) : combinedSpec.and(newSpec);
        }
        useOrLogic = false;
    }

    @Override
    public EntitySpecResult<ENTITY> build() {
        Specification<ENTITY> finalSpec = (combinedSpec == null) ? (root, query, cb) -> cb.conjunction() : combinedSpec;
        return new EntitySpecResult<>(queryStr.toString(), finalSpec);
    }
}