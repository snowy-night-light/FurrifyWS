package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import static ws.furrify.core.specification.EntitySpec.specEquals;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContextUtils {

    private final static String USER_SCOPE_OWNER_VARIABLE_NAME = "ownerId";
    private final static String SERVICE_CLIENT_CLAIM = "service_client";
    private final static String SERVICE_ACCOUNT_CLAIM = "service_account";
    private final static String SERVICE_ACCOUNT_DASH_CLAIM = "service-account";
    private static final InheritableThreadLocal<UUID> OVERRIDE_SUBJECT = new InheritableThreadLocal<>();
    public static final Map<UUID, UUID> FEIGN_FALLBACK_OWNER_MAP = new ConcurrentHashMap<>();

    public static void setOverrideSubject(UUID subject) {
        OVERRIDE_SUBJECT.set(subject);
    }

    public static void clearOverrideSubject() {
        OVERRIDE_SUBJECT.remove();
    }

    public static Optional<UUID> getOverrideSubject() {
        return Optional.ofNullable(OVERRIDE_SUBJECT.get());
    }

    public static void mockFeignClientSecurityContext(UUID ownerId) {
        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("sub", ownerId.toString())
                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                .claim("preferred_username", "service-account-worker")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_admin"), new SimpleGrantedAuthority("ROLE_service_client")))
        );
        setOverrideSubject(ownerId);
    }

    public static void clearFeignClientSecurityContext() {
        clearOverrideSubject();
        SecurityContextHolder.clearContext();
    }

    public static Optional<Jwt> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }

        return Optional.empty();
    }

    public static Optional<UUID> getCurrentSubject() {
        if (getOverrideSubject().isPresent()) {
            return getOverrideSubject();
        }

        if (isServiceToken()) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                String ownerIdHeader = servletRequestAttributes.getRequest().getHeader("X-Furrify-User-Id");
                if (ownerIdHeader != null) {
                    return Optional.of(UUID.fromString(ownerIdHeader));
                } else {
                    throw new AccessDeniedException("Missing X-Furrify-User-Id header for service client.");
                }
            }
        }
        return SecurityContextUtils.getCurrentUserPrincipal().map(Jwt::getSubject).map(UUID::fromString);
    }

    public static boolean isAdminToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> Objects.requireNonNull(auth.getAuthority()).equalsIgnoreCase("ROLE_admin"));
    }

    public static boolean isServiceToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> {
                    String authority = Objects.requireNonNull(auth.getAuthority());
                    return authority.equalsIgnoreCase("ROLE_" + SERVICE_CLIENT_CLAIM) ||
                           authority.equalsIgnoreCase("ROLE_" + SERVICE_ACCOUNT_CLAIM) ||
                           authority.equalsIgnoreCase("ROLE_" + SERVICE_ACCOUNT_DASH_CLAIM);
                });
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> getUserScopedSecuritySpec() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || isServiceToken() || isAdminToken()) {
            return EntitySpec.unrestricted();
        }

        return EntitySpec.<ENTITY>specBuilder().where(USER_SCOPE_OWNER_VARIABLE_NAME, specEquals(getCurrentSubject().orElseThrow(() -> new IllegalStateException("Current user subject was not found. Cannot construct spec.")))).build();
    }
}
