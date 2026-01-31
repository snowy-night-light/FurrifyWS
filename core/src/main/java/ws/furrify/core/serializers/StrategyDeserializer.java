package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

@JacksonComponent
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