package ws.furrify.core.specification;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

public record EntitySpecResult<ENTITY extends BaseEntity>(String specString, Specification<ENTITY> specification) {
}