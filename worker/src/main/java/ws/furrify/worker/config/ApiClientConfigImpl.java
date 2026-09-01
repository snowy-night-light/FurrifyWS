package ws.furrify.worker.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.furrify.core.config.ApiConfig;

@Configuration
@EnableFeignClients(basePackages = {
        "ws.furrify.openapi.gen",
        "org.openapitools.configuration"
})
class ApiClientConfigImpl extends ApiConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}