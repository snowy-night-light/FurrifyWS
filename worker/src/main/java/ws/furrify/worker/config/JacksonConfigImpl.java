package ws.furrify.worker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.config.JacksonConfig;
import ws.furrify.core.serializers.PluginJacksonModule;
import ws.furrify.core.serializers.StrategyJacksonModule;

import java.util.List;

@Configuration
class JacksonConfigImpl extends JacksonConfig {

    @Autowired
    public JacksonConfigImpl(StrategyJacksonModule strategyJacksonModule, PluginJacksonModule pluginJacksonModule, List<ValueSerializer<?>> serializers, List<ValueDeserializer<?>> deserializers) {
        super(strategyJacksonModule, pluginJacksonModule, serializers, deserializers);
    }
}
