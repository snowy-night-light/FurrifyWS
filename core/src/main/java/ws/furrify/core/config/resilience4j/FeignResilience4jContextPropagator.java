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