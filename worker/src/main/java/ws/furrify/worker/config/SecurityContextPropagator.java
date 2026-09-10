package ws.furrify.worker.config;

import io.github.resilience4j.core.ContextPropagator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class SecurityContextPropagator implements ContextPropagator<Object[]> {

    @Override
    public Supplier<Optional<Object[]>> retrieve() {
        return () -> {
            SecurityContext context = SecurityContextHolder.getContext();
            UUID overrideSubject = SecurityContextUtils.getOverrideSubject().orElse(null);
            return Optional.of(new Object[]{context, overrideSubject});
        };
    }

    @Override
    public Consumer<Optional<Object[]>> copy() {
        return (opt) -> {
            if (opt.isPresent()) {
                Object[] arr = opt.get();
                if (arr[0] != null) {
                    SecurityContextHolder.setContext((SecurityContext) arr[0]);
                }
                if (arr[1] != null) {
                    SecurityContextUtils.setOverrideSubject((UUID) arr[1]);
                } else {
                    SecurityContextUtils.clearOverrideSubject();
                }
            }
        };
    }

    @Override
    public Consumer<Optional<Object[]>> clear() {
        return (opt) -> {
            SecurityContextHolder.clearContext();
            SecurityContextUtils.clearOverrideSubject();
        };
    }
}
