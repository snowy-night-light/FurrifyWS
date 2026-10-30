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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import ws.furrify.core.utils.AsyncUtils;

import java.util.concurrent.ThreadPoolExecutor;

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

    @Bean(name = "applicationTaskExecutor")
    @Primary
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int processors = Runtime.getRuntime().availableProcessors();

        int corePoolSize = processors * 2;
        int maxPoolSize = processors * 4;

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(100);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("furrify-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());

        executor.initialize();
        return executor;
    }
}