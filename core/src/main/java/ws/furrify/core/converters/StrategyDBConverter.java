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