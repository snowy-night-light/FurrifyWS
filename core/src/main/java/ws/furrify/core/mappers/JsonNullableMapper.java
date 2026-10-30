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

import org.mapstruct.Condition;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

@Component
public class JsonNullableMapper {

    @Condition
    public <T> boolean isPresent(JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }

    public <T> T unwrap(JsonNullable<T> nullable) {
        return nullable == null ? null : nullable.orElse(null);
    }
}