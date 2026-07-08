package ws.furrify.storage.domain.source.strategy;

import ws.furrify.core.model.StrategyIntf;

import java.util.Map;

/**
 * Source strategy used to manage and update content from content providers.
 */
public interface SourceStrategy extends StrategyIntf {

    boolean validateData(Map<String, Object> data);
}
