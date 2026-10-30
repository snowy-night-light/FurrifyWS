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