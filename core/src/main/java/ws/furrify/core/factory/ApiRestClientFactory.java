package ws.furrify.core.factory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Lazy
public class ApiRestClientFactory {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestClient.Builder loadBalancedBuilder;

    private final static String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    private final static String PRINCIPAL_SUFFIX = "-client";

    private final String principalName;

    public ApiRestClientFactory(OAuth2AuthorizedClientManager authorizedClientManager,
                                @Qualifier("loadBalancedBuilder") RestClient.Builder loadBalancedBuilder,
                                @Value("${spring.application.name}") String appName) {
        this.authorizedClientManager = authorizedClientManager;
        this.loadBalancedBuilder = loadBalancedBuilder;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    /**
     * Builds a RestClient using confidential JWT for service-to-service communication through eureka.
     */
    public RestClient buildSecureClient(String baseUrl) {

        ClientHttpRequestInterceptor oauthInterceptor = (request, body, execution) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                    .principal(principalName)
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
            }

            return execution.execute(request, body);
        };

        return loadBalancedBuilder.clone()
                .requestInterceptor(oauthInterceptor)
                .baseUrl(baseUrl)
                .build();
    }
}