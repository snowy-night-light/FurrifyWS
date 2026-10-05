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
package ws.furrify.core.serializers;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.Serializers;
import ws.furrify.core.model.StrategyIntf;

@Configuration
@RequiredArgsConstructor
public class StrategyJacksonModule {

    private final StrategySerializer strategySerializer;
    private final StrategyDeserializer strategyDeserializer;

    @Bean
    public SimpleModule strategyHierarchyModule() {
        return new SimpleModule("StrategyHierarchyModule") {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);

                context.addDeserializers(new Deserializers.Base() {
                    @Override
                    public ValueDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
                        if (StrategyIntf.class.isAssignableFrom(type.getRawClass())) {
                            return strategyDeserializer;
                        }
                        return null;
                    }

                    @Override
                    public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
                        return StrategyIntf.class.isAssignableFrom(valueType);
                    }
                });

                context.addSerializers(new Serializers.Base() {
                    @Override
                    public ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
                        if (StrategyIntf.class.isAssignableFrom(type.getRawClass())) {
                            return strategySerializer;
                        }
                        return null;
                    }
                });
            }
        };
    }
}