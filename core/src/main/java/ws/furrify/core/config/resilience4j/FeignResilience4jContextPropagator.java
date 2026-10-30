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
package ws.furrify.core.config.resilience4j;

import io.github.resilience4j.core.ContextPropagator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FeignResilience4jContextPropagator implements ContextPropagator<FeignResilience4jContextPropagator.PropagatedState> {

    public record PropagatedState(
            RequestAttributes requestAttributes,
            SecurityContext securityContext
    ) {}

    @Override
    public Supplier<Optional<PropagatedState>> retrieve() {
        return () -> Optional.of(new PropagatedState(
                RequestContextHolder.getRequestAttributes(),
                SecurityContextHolder.getContext()
        ));
    }

    @Override
    public Consumer<Optional<PropagatedState>> copy() {
        return opt -> {
            if (opt.isPresent()) {
                PropagatedState state = opt.get();

                if (state.requestAttributes() != null) {
                    RequestContextHolder.setRequestAttributes(state.requestAttributes());
                }

                if (state.securityContext() != null) {
                    SecurityContextHolder.setContext(state.securityContext());
                }
            }
        };
    }

    @Override
    public Consumer<Optional<PropagatedState>> clear() {
        return opt -> {
            RequestContextHolder.resetRequestAttributes();
            SecurityContextHolder.clearContext();
        };
    }
}