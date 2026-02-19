package ws.furrify.core.config;

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
class KeycloakRoleConverter implements GrantedAuthoritiesMapper, Converter<Jwt, Collection<GrantedAuthority>> {

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

        // Map realm roles

        final Object realmAccessObj = claims.get("realm_access");
        if (!(realmAccessObj instanceof Map<?, ?>)) {
            return grantedAuthorities;
        }
        final Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;

        final Object realmRolesObj = realmAccess.get("roles");
        if (!(realmRolesObj instanceof List<?>)) {
            return grantedAuthorities;
        }
        final List<String> realmRoles = (List<String>) realmRolesObj;

        for (String role : realmRoles) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // Map resource roles

        final Object resourceAccessObj = claims.get("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?>)) {
            return grantedAuthorities;
        }
        final Map<String, Object> resourceAccess = (Map<String, Object>) resourceAccessObj;

        for (Map.Entry<String, Object> resource : resourceAccess.entrySet()) {
            final String resourceName = resource.getKey();

            final Object resourceDetailsObj = resource.getValue();
            if (!(resourceDetailsObj instanceof Map<?, ?>)) {
                continue;
            }
            final Map<String, Object> resourceDetails = (Map<String, Object>) resourceDetailsObj;

            final Object rolesObj = resourceDetails.get("roles");
            if (!(rolesObj instanceof List<?>)) {
                continue;
            }
            final List<String> resourceRole = (List<String>) rolesObj;

            for (String role : resourceRole) {
                grantedAuthorities.add(new SimpleGrantedAuthority(resourceName + "_" + role));
            }
        }

        return grantedAuthorities;
    }
}
