/*
 * furrify-core - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.core.serializers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

@Component
@RequiredArgsConstructor
public class StrategySerializer extends ValueSerializer<StrategyIntf> {

    private final StrategyRegistryService strategyRegistryService;

    @Override
    public void serialize(StrategyIntf strategyIntf, JsonGenerator jsonGenerator, SerializationContext context) throws JacksonException {
        jsonGenerator.writeString(
                strategyRegistryService.serializeStrategy(strategyIntf)
        );
    }
}
