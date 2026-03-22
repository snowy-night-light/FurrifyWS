package ws.furrify.testcore.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TestConfiguration
public class AuthorizationTestConfig {

    public static final UUID MOCK_SUBJECT_ID = UUID.fromString("ab715cdc-4bc6-4297-b9bd-167897ea9e22");

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(MOCK_SUBJECT_ID.toString())
                .claim("realm_access", Map.of("roles", List.of("admin", "user")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}