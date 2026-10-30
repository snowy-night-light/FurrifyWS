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