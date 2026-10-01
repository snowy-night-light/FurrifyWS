package ws.furrify.core.config.resilience4j;

import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.UUID;

public class SecurityContextThreadLocalAccessor implements ThreadLocalAccessor<Object[]> {

    public static final String KEY = "furrifySecurityContext";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public Object[] getValue() {
        return new Object[]{
                SecurityContextHolder.getContext(),
                SecurityContextUtils.getOverrideSubject().orElse(null)
        };
    }

    @Override
    public void setValue(Object[] value) {
        if (value != null) {
            if (value[0] != null) {
                SecurityContextHolder.setContext((SecurityContext) value[0]);
            }
            if (value[1] != null) {
                SecurityContextUtils.setOverrideSubject((UUID) value[1]);
            } else {
                SecurityContextUtils.clearOverrideSubject();
            }
        }
    }

    @Override
    public void restore() {
        SecurityContextHolder.clearContext();
        SecurityContextUtils.clearOverrideSubject();
    }

    @Override
    public void restore(Object[] previousValue) {
        setValue(previousValue);
    }
}
