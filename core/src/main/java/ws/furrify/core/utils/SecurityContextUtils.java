package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import java.util.Optional;
import java.util.UUID;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContextUtils {

    private final static String USER_SCOPE_OWNER_VARIABLE_NAME = "ownerId";

    public static Optional<Jwt> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }

        return Optional.empty();
    }

    public static Optional<UUID> getCurrentSubject() {
        return SecurityContextUtils.getCurrentUserPrincipal().map(Jwt::getSubject).map(UUID::fromString);
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> getUserScopedSecuritySpec() {
        return EntitySpec.<ENTITY>specBuilder().where(USER_SCOPE_OWNER_VARIABLE_NAME, specEquals(getCurrentSubject().orElseThrow(() -> new IllegalStateException("Current user subject was not found. Cannot construct spec.")))).build();
    }
}
