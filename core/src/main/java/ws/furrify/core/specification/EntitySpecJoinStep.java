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