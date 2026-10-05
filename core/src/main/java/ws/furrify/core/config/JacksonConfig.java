/*
 * furrify-core - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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