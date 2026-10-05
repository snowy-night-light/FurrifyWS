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

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonNullableConverterFactory implements ConverterFactory<Object, JsonNullable<?>> {

    @Override
    public <T extends JsonNullable<?>> Converter<Object, T> getConverter(Class<T> targetType) {
        return new JsonNullableConverter();
    }

    private static class JsonNullableConverter<T> implements Converter<Object, JsonNullable<T>> {
        @Override
        public JsonNullable<T> convert(Object source) {
            return JsonNullable.of((T) source);
        }
    }
}