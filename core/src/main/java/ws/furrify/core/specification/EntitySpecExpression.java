package ws.furrify.core.specification;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

public interface EntitySpecExpression<ENTITY extends BaseEntity> {
    String expression();
    Specification<ENTITY> toSpecification(String field);
}