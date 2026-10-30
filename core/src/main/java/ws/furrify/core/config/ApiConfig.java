/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.furrify.core.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import ws.furrify.core.config.feign.OAuth2FeignErrorDecoder;
import ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor;
import ws.furrify.core.config.micrometer.RequestContextThreadLocalAccessor;
import ws.furrify.core.config.micrometer.SecurityContextThreadLocalAccessor;
import ws.furrify.core.config.resilience4j.FeignResilience4jContextPropagator;
import ws.furrify.core.service.ContextPropagatingScheduledExecutorService;

import java.util.concurrent.Executors;

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
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerFactoryCustomizer() {
        return factory -> factory.configureExecutorService(
                new DelegatingSecurityContextExecutorService(Executors.newCachedThreadPool())
        );
    }
    @PostConstruct
    public void registerThreadLocalAccessors() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new RequestContextThreadLocalAccessor());
        ContextRegistry.getInstance().registerThreadLocalAccessor(new SecurityContextThreadLocalAccessor());
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }

    @Bean
    public FeignResilience4jContextPropagator feignResilience4jContextPropagator() {
        return new FeignResilience4jContextPropagator();
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> resilience4jCircuitBreakerCustomizer() {
        return factory -> {
            int processors = Runtime.getRuntime().availableProcessors();

            int corePoolSize = processors * 4;

            factory.configureExecutorService(
                    new ContextPropagatingScheduledExecutorService(
                            Executors.newScheduledThreadPool(corePoolSize)
                    )
            );

            factory.configureGroupExecutorService(groupName ->
                    new ContextPropagatingScheduledExecutorService(
                            Executors.newScheduledThreadPool(corePoolSize)
                    )
            );
        };
    }
}