package ws.furrify.core.specification;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

import java.util.function.BiFunction;

record InternalExpression<ENTITY extends BaseEntity>(
        String expression,
        BiFunction<String, Object, Specification<ENTITY>> specProvider,
        Object value,
        Specification<ENTITY> nestedSpec
) implements EntitySpecExpression<ENTITY> {
    @Override
    public Specification<ENTITY> toSpecification(String field) {
        return (nestedSpec != null) ? nestedSpec : specProvider.apply(field, value);
    }
}