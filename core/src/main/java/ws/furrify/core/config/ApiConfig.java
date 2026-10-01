package ws.furrify.core.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import ws.furrify.core.config.feign.OAuth2FeignErrorDecoder;
import ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor;

public abstract class ApiConfig {
    @Bean
    public SmartOAuth2FeignRequestInterceptor smartOAuth2FeignRequestInterceptor(
            @Qualifier("serviceOAuth2AuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${spring.application.name}") String appName) {

        return new SmartOAuth2FeignRequestInterceptor(authorizedClientManager, appName);
    }

    @Bean
    public ErrorDecoder oauth2FeignErrorDecoder(
            OAuth2AuthorizedClientService authorizedClientService,
            @Value("${spring.application.name}") String appName) {

        return new OAuth2FeignErrorDecoder(authorizedClientService, appName);
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
