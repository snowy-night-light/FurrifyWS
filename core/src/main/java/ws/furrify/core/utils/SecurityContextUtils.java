package ws.furrify.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContextUtils {

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
}
