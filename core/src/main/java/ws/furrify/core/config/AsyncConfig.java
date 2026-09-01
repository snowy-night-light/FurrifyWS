package ws.furrify.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import ws.furrify.core.utils.AsyncUtils;

@EnableAsync
public class AsyncConfig {
    @Bean
    public AsyncUtils asyncUtils() {
        return new AsyncUtils();
    }
}