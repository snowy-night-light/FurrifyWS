package ws.furrify.core.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ws.furrify.core.utils.SecurityContextUtils;

public class SmartOAuth2FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SmartOAuth2FeignRequestInterceptor.class);

    protected static final String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    protected static final String PRINCIPAL_SUFFIX = "-client";

    private final OAuth2AuthorizedClientManager serviceOAuth2AuthorizedClientManager;
    private final String principalName;

    public SmartOAuth2FeignRequestInterceptor(
            OAuth2AuthorizedClientManager serviceOAuth2AuthorizedClientManager,
            String appName) {

        this.serviceOAuth2AuthorizedClientManager = serviceOAuth2AuthorizedClientManager;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes =
                RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {

            String authorization =
                    servletRequestAttributes.getRequest()
                            .getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization != null && authorization.startsWith("Bearer ")) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
                return;
            }
        }

        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                        .principal(principalName)
                        .build();

        OAuth2AuthorizedClient authorizedClient = null;
        int attempts = 0;
        int maxAttempts = 3;
        while (attempts < maxAttempts) {
            try {
                authorizedClient = serviceOAuth2AuthorizedClientManager.authorize(authorizeRequest);
                break;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw e;
                }
                log.warn("Failed to obtain OAuth2 token (attempt {}/{}). Retrying... Cause: {}", attempts, maxAttempts, e.getMessage());
                try {
                    Thread.sleep((long) 1000 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting to retry OAuth2 authorization", ie);
                }
            }
        }

        if (authorizedClient == null) {
            throw new IllegalStateException(
                    "Unable to obtain service access token for client '"
                            + KEYCLOAK_INTERNAL_CONFIG_ID + "'"
            );
        }

        template.header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + authorizedClient.getAccessToken().getTokenValue()
        );

        SecurityContextUtils.getCurrentSubject().ifPresent(subject ->
                template.header("X-Furrify-User-Id", subject.toString())
        );

        if (!template.headers().containsKey("X-Furrify-User-Id") && template.body() != null) {
            String bodyString = new String(template.body());
            Matcher matcher = Pattern.compile("\"sourceBookReferenceId\"\\s*:\\s*\"([^\"]+)\"").matcher(bodyString);
            if (matcher.find()) {
                try {
                    UUID bookId = UUID.fromString(matcher.group(1));
                    UUID ownerId = SecurityContextUtils.FEIGN_FALLBACK_OWNER_MAP.get(bookId);
                    if (ownerId != null) {
                        template.header("X-Furrify-User-Id", ownerId.toString());
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}