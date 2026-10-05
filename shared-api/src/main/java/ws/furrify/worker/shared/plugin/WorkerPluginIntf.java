/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
