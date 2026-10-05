/*
 * furrify-core - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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