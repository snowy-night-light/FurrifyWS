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
public class JacksonConfig {

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