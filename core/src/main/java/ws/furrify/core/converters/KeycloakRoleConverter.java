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
package ws.furrify.core.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extract roles assigned to user in JWT token.
 *
 * @author sky
 */
public class KeycloakRoleConverter implements GrantedAuthoritiesMapper, Converter<Jwt, Collection<GrantedAuthority>> {

    private final static String RESOURCES_KEY = "resource_access";

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(final Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            assert role != null;
            if (role.startsWith("ROLE_")) {
                role = role.substring("ROLE_".length());
            }
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return mappedAuthorities;
    }

    @Override
    public Collection<GrantedAuthority> convert(final Jwt source) {
        final Map<String, Object> claims = source.getClaims();

        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        final Object rootRolesObj = claims.get("roles");
        if (rootRolesObj instanceof List<?> rootRoles) {
            for (Object roleObj : rootRoles) {
                if (roleObj instanceof String role) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }

        final Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            final Object realmRolesObj = realmAccess.get("roles");
            if (realmRolesObj instanceof List<?> realmRoles) {
                for (Object roleObj : realmRoles) {
                    if (roleObj instanceof String role) {
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
        }

        final Object resourceAccessObj = claims.get("resource_access");
        if (resourceAccessObj instanceof Map<?, ?> resourceAccess) {
            for (Map.Entry<?, ?> resource : resourceAccess.entrySet()) {
                final String resourceName = String.valueOf(resource.getKey());

                final Object resourceDetailsObj = resource.getValue();
                if (!(resourceDetailsObj instanceof Map<?, ?> resourceDetails)) {
                    continue;
                }

                final Object rolesObj = resourceDetails.get("roles");
                if (!(rolesObj instanceof List<?> resourceRole)) {
                    continue;
                }

                for (Object roleObj : resourceRole) {
                    if (roleObj instanceof String role) {
                        grantedAuthorities.add(new SimpleGrantedAuthority(resourceName + "_" + role));
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
        }

        return grantedAuthorities;
    }
}
