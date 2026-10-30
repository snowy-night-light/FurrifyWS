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