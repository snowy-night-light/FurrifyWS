package ws.furrify.core.config.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SmartOAuth2FeignRequestInterceptor implements RequestInterceptor {

    private static final String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    private static final String PRINCIPAL_SUFFIX = "-client";

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

        // 1. Try to propagate the user's HTTP token.
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

        // 2. No servlet request / no user token -> use service client.
        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                        .principal(principalName)
                        .build();

        OAuth2AuthorizedClient authorizedClient =
                serviceOAuth2AuthorizedClientManager.authorize(authorizeRequest);

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
    }
}