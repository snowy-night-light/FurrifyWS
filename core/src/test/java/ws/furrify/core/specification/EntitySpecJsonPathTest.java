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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntitySpecJsonPathTest {

    @Test
    void plainKeysAreDotted() {
        assertEquals("$.url", EntitySpec.jsonPath(new String[]{"url"}));
        assertEquals("$.meta.key_2", EntitySpec.jsonPath(new String[]{"meta", "key_2"}));
    }

    @Test
    void otherKeysAreQuoted() {
        assertEquals("$.\"my key\"", EntitySpec.jsonPath(new String[]{"my key"}));
        assertEquals("$.\"1st\"", EntitySpec.jsonPath(new String[]{"1st"}));
        assertEquals("$.\"a\\\"b\"", EntitySpec.jsonPath(new String[]{"a\"b"}));
    }
}
