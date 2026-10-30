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
package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

@Component
@RequiredArgsConstructor
public class StrategyDeserializer extends ValueDeserializer<StrategyIntf> {

    private final StrategyRegistryService strategyRegistryService;

    @Override
    public StrategyIntf deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(jsonParser);
        String strategyName = node.asString();

        return strategyRegistryService.deserializeStrategy(strategyName);
    }
}