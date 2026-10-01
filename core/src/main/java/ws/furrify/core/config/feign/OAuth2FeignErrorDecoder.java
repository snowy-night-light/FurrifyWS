package ws.furrify.core.config.feign;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import static ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor.KEYCLOAK_INTERNAL_CONFIG_ID;
import static ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor.PRINCIPAL_SUFFIX;

public class OAuth2FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String principalName;

    public OAuth2FeignErrorDecoder(OAuth2AuthorizedClientService authorizedClientService, String appName) {
        this.authorizedClientService = authorizedClientService;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 401) {
            authorizedClientService.removeAuthorizedClient(KEYCLOAK_INTERNAL_CONFIG_ID, principalName);

            return new RetryableException(
                    response.status(),
                    "Unauthorized",
                    response.request().httpMethod(),
                    (Long) null,
                    response.request()
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}