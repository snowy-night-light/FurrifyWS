package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.model.PluginIntf;
import ws.furrify.core.service.PluginRegistryService;

@Component
@RequiredArgsConstructor
public class PluginSerializer extends ValueSerializer<PluginIntf> {

    private final PluginRegistryService pluginRegistryService;

    @Override
    public void serialize(PluginIntf pluginIntf, JsonGenerator jsonGenerator, SerializationContext context) throws JacksonException {
        jsonGenerator.writeString(
                pluginRegistryService.serializePlugin(pluginIntf)
        );
    }
}
