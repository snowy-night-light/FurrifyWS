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

import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import ws.furrify.core.model.StrategyIntf;

import java.util.Collections;

@RequiredArgsConstructor
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(StrategyIntf.class, String.class);
    }

    @Bean
    public ObjectMapperProvider springdocObjectMapperProvider(SpringDocConfigProperties springDocConfigProperties) {
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider(springDocConfigProperties);
        objectMapperProvider.jsonMapper().registerModule(new JsonNullableModule());
        return objectMapperProvider;
    }

    @Bean
    public PropertyCustomizer jsonNullablePropertyCustomizer() {
        return (schema, type) -> {
            if (schema != null && type != null && type.getType() != null &&
                    type.getType().getTypeName().contains("org.openapitools.jackson.nullable.JsonNullable")) {
                if (schema.get$ref() != null) {
                    Schema<?> refSchema = new Schema<>();
                    refSchema.set$ref(schema.get$ref());
                    schema.set$ref(null);
                    schema.setAllOf(Collections.singletonList(refSchema));
                }
                schema.setNullable(true);
            }
            return schema;
        };
    }
}
