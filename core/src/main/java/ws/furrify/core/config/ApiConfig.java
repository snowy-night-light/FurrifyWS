package ws.furrify.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import ws.furrify.core.config.interceptor.SmartOAuth2FeignRequestInterceptor;
import feign.Retryer;

public abstract class ApiConfig {
    @Bean
    public SmartOAuth2FeignRequestInterceptor smartOAuth2FeignRequestInterceptor(
            @Qualifier("serviceOAuth2AuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${spring.application.name}") String appName) {

        return new SmartOAuth2FeignRequestInterceptor(authorizedClientManager, appName);
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
