package ws.furrify.core.serializers;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.Serializers;
import ws.furrify.core.model.PluginIntf;

@Configuration
@RequiredArgsConstructor
public class PluginJacksonModule {

    private final PluginSerializer pluginSerializer;
    private final PluginDeserializer pluginDeserializer;

    @Bean
    public SimpleModule pluginHierarchyModule() {
        return new SimpleModule("PluginHierarchyModule") {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);

                context.addDeserializers(new Deserializers.Base() {
                    @Override
                    public ValueDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
                        if (PluginIntf.class.isAssignableFrom(type.getRawClass())) {
                            return pluginDeserializer;
                        }
                        return null;
                    }

                    @Override
                    public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
                        return PluginIntf.class.isAssignableFrom(valueType);
                    }
                });

                context.addSerializers(new Serializers.Base() {
                    @Override
                    public ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
                        if (PluginIntf.class.isAssignableFrom(type.getRawClass())) {
                            return pluginSerializer;
                        }
                        return null;
                    }
                });
            }
        };
    }
}