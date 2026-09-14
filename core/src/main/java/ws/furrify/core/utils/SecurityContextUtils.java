package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContextUtils {

    private final static String USER_SCOPE_OWNER_VARIABLE_NAME = "ownerId";
    private final static String SERVICE_CLIENT_CLAIM = "service_client";
    private static final InheritableThreadLocal<UUID> OVERRIDE_SUBJECT = new InheritableThreadLocal<>();

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
                new JwtAuthenticationToken(jwt, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin"), new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_service_client")))
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

        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> Objects.requireNonNull(auth.getAuthority()).equalsIgnoreCase("ROLE_" + SERVICE_CLIENT_CLAIM));

        if (hasRole) {
            return true;
        }

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && preferredUsername.startsWith("service-account-")) {
                return true;
            }
            String azp = jwt.getClaimAsString("azp");
            if (azp != null && azp.equals("keycloak-internal")) {
                return true;
            }
        }

        return false;
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> getUserScopedSecuritySpec() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || isServiceToken() || isAdminToken()) {
            return EntitySpec.unrestricted();
        }

        return EntitySpec.<ENTITY>specBuilder().where(USER_SCOPE_OWNER_VARIABLE_NAME, specEquals(getCurrentSubject().orElseThrow(() -> new IllegalStateException("Current user subject was not found. Cannot construct spec.")))).build();
    }
}
