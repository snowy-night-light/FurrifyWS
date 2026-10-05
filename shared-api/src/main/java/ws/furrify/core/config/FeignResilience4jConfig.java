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

import io.github.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FeignResilience4jConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> resilience4jCircuitBreakerFactoryCustomizer() {
        return factory -> factory.configureExecutorService(
                ContextAwareScheduledThreadPoolExecutor.newScheduledThreadPool()
                        .corePoolSize(10)
                        .build()
        );
    }
}
