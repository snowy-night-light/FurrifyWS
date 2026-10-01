package ws.furrify.worker.shared.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import ws.furrify.core.model.PluginIntf;
import ws.furrify.worker.model.WorkerPluginResults;

@Schema(type = "string")
public interface WorkerPluginIntf extends PluginIntf {
    /**
     * @return Unique plugin source identifier.
     */
    String getProviderName();

    /**
     * Returns the current status of the plugin.
     */
    WorkerPluginResults trackCurrentStatus();
}
