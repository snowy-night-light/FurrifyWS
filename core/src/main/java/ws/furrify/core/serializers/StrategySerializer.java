package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

@JacksonComponent
@RequiredArgsConstructor
public class StrategySerializer extends ValueSerializer<StrategyIntf> {

    private final StrategyRegistryService strategyRegistryService;

    @Override
    public void serialize(StrategyIntf strategyIntf, JsonGenerator jsonGenerator, SerializationContext context) throws JacksonException {
        jsonGenerator.writeString(
                strategyRegistryService.serializeStrategy(strategyIntf)
        );
    }
}
