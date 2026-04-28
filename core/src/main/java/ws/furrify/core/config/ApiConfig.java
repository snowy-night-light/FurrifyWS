package ws.furrify.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import ws.furrify.core.config.interceptor.OAuth2FeignRequestInterceptor;

@EnableFeignClients(basePackages = {
        "ws.furrify.openapi.gen",
        "org.openapitools.configuration"
})
public abstract class ApiConfig {
    @Bean
    public OAuth2FeignRequestInterceptor oauth2FeignRequestInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${spring.application.name}") String appName) {

        return new OAuth2FeignRequestInterceptor(authorizedClientManager, appName);
    }

}
