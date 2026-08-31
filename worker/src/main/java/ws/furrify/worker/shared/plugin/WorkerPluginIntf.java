package ws.furrify.worker.shared.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import ws.furrify.core.model.PluginIntf;

@Schema(type = "string")
public interface WorkerPluginIntf extends PluginIntf {
}
