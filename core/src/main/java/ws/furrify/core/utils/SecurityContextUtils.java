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
package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

import static ws.furrify.core.specification.EntitySpec.specEquals;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContextUtils {

    public static final String USER_ID_HEADER = "X-Furrify-User-Id";
    private final static String USER_SCOPE_OWNER_VARIABLE_NAME = "ownerId";
    private final static String SERVICE_CLIENT_CLAIM = "service_client";
    private final static String SERVICE_ACCOUNT_CLAIM = "service_account";
    private final static String SERVICE_ACCOUNT_DASH_CLAIM = "service-account";

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
    }

    public static void clearFeignClientSecurityContext() {
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
        if (isServiceToken()) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                String ownerIdHeader = servletRequestAttributes.getRequest().getHeader(USER_ID_HEADER);
                if (ownerIdHeader != null) {
                    return Optional.of(UUID.fromString(ownerIdHeader));
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