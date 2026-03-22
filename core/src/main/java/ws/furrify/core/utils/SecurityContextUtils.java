package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import ws.furrify.core.entity.BaseEntity;

import java.util.Optional;
import java.util.UUID;
import static ws.furrify.core.utils.EntitySpecBuilder.*;

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

    public static <ENTITY extends BaseEntity> Specification<ENTITY> getUserScopedSecuritySpec() {
        return EntitySpecBuilder.<ENTITY>specBuilder().where(USER_SCOPE_OWNER_VARIABLE_NAME, specEquals(getCurrentSubject().orElseThrow(() -> new IllegalStateException("Current user subject was not found. Cannot construct spec.")))).build().specification();
    }
}
