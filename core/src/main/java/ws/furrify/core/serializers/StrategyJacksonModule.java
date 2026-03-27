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