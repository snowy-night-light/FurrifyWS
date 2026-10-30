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