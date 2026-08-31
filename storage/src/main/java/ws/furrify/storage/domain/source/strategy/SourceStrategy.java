package ws.furrify.storage.domain.source.strategy;

import ws.furrify.core.model.StrategyIntf;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Source strategy used to manage and update content from content providers.
 */
@Schema(type = "string")
public interface SourceStrategy extends StrategyIntf {

    boolean validateData(Map<String, Object> data);
}
