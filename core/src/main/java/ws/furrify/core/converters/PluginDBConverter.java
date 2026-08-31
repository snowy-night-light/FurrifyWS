package ws.furrify.core.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ws.furrify.core.model.PluginIntf;
import ws.furrify.core.service.PluginRegistryService;

@Converter
public class PluginDBConverter implements AttributeConverter<PluginIntf, String> {

    @Override
    public String convertToDatabaseColumn(PluginIntf strategy) {
        if (strategy == null)  {
            return null;
        }

        return PluginRegistryService.getInstance().serializePlugin(strategy);
    }

    @Override
    public PluginIntf convertToEntityAttribute(String strategy) {
        if (strategy == null) {
            return null;
        }

        return PluginRegistryService.getInstance().deserializePlugin(strategy);
    }
}