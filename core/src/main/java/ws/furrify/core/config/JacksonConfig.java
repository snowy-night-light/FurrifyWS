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
package ws.furrify.core.config;

import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import ws.furrify.core.serializers.StrategyJacksonModule;

import java.util.List;

@RequiredArgsConstructor
public abstract class JacksonConfig {

    private final StrategyJacksonModule strategyJacksonModule;
    private final List<ValueSerializer<?>> serializers;
    private final List<ValueDeserializer<?>> deserializers;

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JsonMapper jacksonJsonMapper() {
        SimpleModule componentModule = new SimpleModule();

        for (ValueSerializer<?> serializer : serializers) {
            Class<?> type = ResolvableType.forInstance(serializer).as(ValueSerializer.class).getGeneric(0).resolve();
            if (type != null) {
                componentModule.addSerializer((Class) type, (ValueSerializer) serializer);
            }
        }

        for (ValueDeserializer<?> deserializer : deserializers) {
            Class<?> type = ResolvableType.forInstance(deserializer).as(ValueDeserializer.class).getGeneric(0).resolve();
            if (type != null) {
                componentModule.addDeserializer((Class) type, (ValueDeserializer) deserializer);
            }
        }

        return JsonMapper.builder()
                .addModule(strategyJacksonModule.strategyHierarchyModule())
                .addModule(new JsonNullableJackson3Module())
                .addModule(componentModule)
                .build();
    }
}