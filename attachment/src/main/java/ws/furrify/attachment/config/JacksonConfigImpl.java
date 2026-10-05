/*
 * furrify-attachment-service - Furrify Workspace Project
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
package ws.furrify.attachment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import ws.furrify.core.config.JacksonConfig;
import ws.furrify.core.serializers.StrategyJacksonModule;

import java.util.List;

@Configuration
class JacksonConfigImpl extends JacksonConfig {

    @Autowired
    public JacksonConfigImpl(StrategyJacksonModule strategyJacksonModule, List<ValueSerializer<?>> serializers, List<ValueDeserializer<?>> deserializers) {
        super(strategyJacksonModule, serializers, deserializers);
    }
}
