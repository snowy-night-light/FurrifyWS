package ws.furrify.storage.mocks;

import org.springframework.stereotype.Component;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

import java.util.Map;

@Component
public class MockSourceStrategyImpl implements SourceStrategy {
    @Override
    public boolean validateData(Map<String, Object> data) {
        return true;
    }
}
