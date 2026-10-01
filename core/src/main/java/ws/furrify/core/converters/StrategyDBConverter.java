package ws.furrify.core.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

@Converter
public class StrategyDBConverter implements AttributeConverter<StrategyIntf, String> {

    @Override
    public String convertToDatabaseColumn(StrategyIntf strategy) {
        if (strategy == null)  {
            return null;
        }

        return StrategyRegistryService.getInstance().serializeStrategy(strategy);
    }

    @Override
    public StrategyIntf convertToEntityAttribute(String strategy) {
        if (strategy == null) {
            return null;
        }

        return StrategyRegistryService.getInstance().deserializeStrategy(strategy);
    }
}