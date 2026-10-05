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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextThreadLocalAccessor implements ThreadLocalAccessor<SecurityContextThreadLocalAccessor.SecurityState> {

    public static final String KEY = "furrifySecurityContext";

    public record SecurityState(SecurityContext context) {}

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public SecurityState getValue() {
        return new SecurityState(SecurityContextHolder.getContext());
    }

    @Override
    public void setValue(SecurityState value) {
        if (value != null) {
            if (value.context() != null) {
                SecurityContextHolder.setContext(value.context());
            }
        }
    }

    @Override
    public void restore() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public void restore(SecurityState previousValue) {
        setValue(previousValue);
    }
}