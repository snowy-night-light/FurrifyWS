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
package ws.furrify.storage.domain.source.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import ws.furrify.core.model.StrategyIntf;

import java.util.Map;

/**
 * Source strategy used to manage and update content from content providers.
 */
@Schema(type = "string")
public interface SourceStrategy extends StrategyIntf {

    boolean validateData(Map<String, Object> data);
}
