package ws.furrify.storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ws.furrify.core.config.ApiClientConfig;
import ws.furrify.core.factory.ApiRestClientFactory;
import ws.furrify.openapi.gen.common.ApiClient;

@Configuration
class ApiClientConfigImpl extends ApiClientConfig {

        @Bean
        public ApiClient attachmentApiClient(ApiRestClientFactory restClientFactory) {
            String serviceUrl = "http://furrify-attachment-service";

            RestClient restClient = restClientFactory.buildSecureClient(serviceUrl);

            ApiClient apiClient = new ApiClient(restClient);
            apiClient.setBasePath(serviceUrl);

            return apiClient;
        }

}