package ws.furrify.core.security.annotation;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom security annotation that restricts access to the service client role
 * or an admin role, bypassing the need to constantly redefine both.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("hasAnyRole('service_client', 'admin')")
public @interface ServiceClientOnlySecured {
}
