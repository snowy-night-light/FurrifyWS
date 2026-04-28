package ws.furrify.core.config.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final static String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    private final static String PRINCIPAL_SUFFIX = "-client";
    private final String principalName;

    public OAuth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager, String appName) {
        this.authorizedClientManager = authorizedClientManager;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    /**
     * Builds a RestClient using confidential JWT for service-to-service communication through eureka.
     */
    @Override
    public void apply(RequestTemplate template) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                .principal(principalName)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            template.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        }
    }
}