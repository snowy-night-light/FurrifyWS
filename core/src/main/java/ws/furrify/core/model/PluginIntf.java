package ws.furrify.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.pf4j.ExtensionPoint;

@Schema(type = "string")
public interface PluginIntf extends ExtensionPoint {
}
