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
package ws.furrify.core.config.micrometer;

import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestContextThreadLocalAccessor implements ThreadLocalAccessor<RequestAttributes> {

    public static final String KEY = "furrifyRequestContext";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public RequestAttributes getValue() {
        return RequestContextHolder.getRequestAttributes();
    }

    @Override
    public void setValue(RequestAttributes value) {
        if (value != null) {
            RequestContextHolder.setRequestAttributes(value);
        }
    }

    @Override
    public void restore() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Override
    public void restore(RequestAttributes previousValue) {
        setValue(previousValue);
    }
}