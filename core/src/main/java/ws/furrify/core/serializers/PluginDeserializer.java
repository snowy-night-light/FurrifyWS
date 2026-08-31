package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import ws.furrify.core.model.PluginIntf;
import ws.furrify.core.service.PluginRegistryService;

@Component
@RequiredArgsConstructor
public class PluginDeserializer extends ValueDeserializer<PluginIntf> {

    private final PluginRegistryService plluginRegistryService;

    @Override
    public PluginIntf deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(jsonParser);
        String plluginName = node.asString();

        return plluginRegistryService.deserializePlugin(plluginName);
    }
}