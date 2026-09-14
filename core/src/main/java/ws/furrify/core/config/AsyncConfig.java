package ws.furrify.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import ws.furrify.core.utils.AsyncUtils;

@EnableAsync
@EnableScheduling
public class AsyncConfig {
    @Bean
    public AsyncUtils asyncUtils(
            PlatformTransactionManager transactionManager,
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor
    ) {
        return new AsyncUtils(transactionManager, taskExecutor);
    }
}