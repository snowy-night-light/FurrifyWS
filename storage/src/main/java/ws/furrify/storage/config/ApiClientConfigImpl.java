package ws.furrify.storage.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ws.furrify.core.config.ApiConfig;

@Configuration
@EnableFeignClients(basePackages = {
        "ws.furrify.openapi.gen",
        "org.openapitools.configuration"
})
class ApiClientConfigImpl extends ApiConfig {
}