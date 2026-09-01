package ws.furrify.core.utils;

import org.springframework.scheduling.annotation.Async;

public class AsyncUtils {
    @Async
    public void runAsync(Runnable task) {
        task.run();
    }
}
