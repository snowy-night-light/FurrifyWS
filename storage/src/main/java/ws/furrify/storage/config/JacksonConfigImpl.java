package ws.furrify.storage.config;

import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.config.JacksonConfig;
import ws.furrify.core.serializers.StrategyJacksonModule;

import java.util.List;

@Configuration
class JacksonConfigImpl extends JacksonConfig {

    public JacksonConfigImpl(StrategyJacksonModule strategyJacksonModule, List<ValueSerializer<?>> serializers, List<ValueDeserializer<?>> deserializers) {
        super(strategyJacksonModule, serializers, deserializers);
    }
}
