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