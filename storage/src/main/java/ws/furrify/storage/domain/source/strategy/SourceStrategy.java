package ws.furrify.storage.domain.source.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.serializers.StrategyDeserializer;
import ws.furrify.core.serializers.StrategySerializer;

import java.util.Map;

/**
 * Source strategy used to manage and update content from content providers.
 */
@Schema(type = "string")
public interface SourceStrategy extends StrategyIntf {

    boolean validateData(Map<String, Object> data);
}
