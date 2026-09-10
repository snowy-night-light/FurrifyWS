package ws.furrify.worker.config;

import io.github.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignResilience4jConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> resilience4jCircuitBreakerFactoryCustomizer(SecurityContextPropagator propagator) {
        return factory -> factory.configureExecutorService(
                ContextAwareScheduledThreadPoolExecutor.newScheduledThreadPool()
                        .corePoolSize(10)
                        .contextPropagators(propagator)
                        .build()
        );
    }
}
