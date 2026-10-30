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
package ws.furrify.core.service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ContextPropagatingScheduledExecutorService implements ScheduledExecutorService {

    private final ScheduledExecutorService delegate;

    public ContextPropagatingScheduledExecutorService(ScheduledExecutorService delegate) {
        this.delegate = delegate;
    }

    private <T> Callable<T> wrap(Callable<T> task) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return () -> {
            try {
                if (requestAttributes != null) RequestContextHolder.setRequestAttributes(requestAttributes);
                if (securityContext != null) SecurityContextHolder.setContext(securityContext);

                return task.call();
            } finally {
                RequestContextHolder.resetRequestAttributes();
                SecurityContextHolder.clearContext();
            }
        };
    }

    private Runnable wrap(Runnable task) {
        Callable<Object> callable = wrap(() -> {
            task.run();
            return null;
        });
        return () -> {
            try { callable.call(); } catch (Exception e) { throw new RuntimeException(e); }
        };
    }

    private <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        return tasks.stream().map(this::wrap).collect(Collectors.toList());
    }

    @Override public void execute(Runnable command) { delegate.execute(wrap(command)); }
    @Override public <T> Future<T> submit(Callable<T> task) { return delegate.submit(wrap(task)); }
    @Override public <T> Future<T> submit(Runnable task, T result) { return delegate.submit(wrap(task), result); }
    @Override public Future<?> submit(Runnable task) { return delegate.submit(wrap(task)); }
    @Override public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) { return delegate.schedule(wrap(command), delay, unit); }
    @Override public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) { return delegate.schedule(wrap(callable), delay, unit); }
    @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) { return delegate.scheduleAtFixedRate(wrap(command), initialDelay, period, unit); }
    @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) { return delegate.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit); }
    @Override public void shutdown() { delegate.shutdown(); }
    @Override public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
    @Override public boolean isShutdown() { return delegate.isShutdown(); }
    @Override public boolean isTerminated() { return delegate.isTerminated(); }
    @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return delegate.awaitTermination(timeout, unit); }
    @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException { return delegate.invokeAll(wrap(tasks)); }
    @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException { return delegate.invokeAll(wrap(tasks), timeout, unit); }
    @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException { return delegate.invokeAny(wrap(tasks)); }
    @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException { return delegate.invokeAny(wrap(tasks), timeout, unit); }
}