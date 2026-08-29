package ws.furrify.core.config.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SmartOAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final static String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    private final static String PRINCIPAL_SUFFIX = "-client";
    private final String principalName;

    public SmartOAuth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager, String appName) {
        this.authorizedClientManager = authorizedClientManager;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String authorization = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
                return;
            }
        }

        // Fallback to Service Token
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                .principal(principalName)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        }
    }
}