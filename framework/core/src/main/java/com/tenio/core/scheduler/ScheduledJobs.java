/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.scheduler;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.Scheduled;
import com.tenio.core.bootstrap.injector.ProfileSelection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Validates schedules before launching any callbacks. Owned by one server scheduler.
 */
public final class ScheduledJobs extends SystemLogger {

    private final List<Consumer<ScheduledThreadPoolExecutor>> plans = new ArrayList<>();
    private final List<String> taskNames = new ArrayList<>();
    private boolean started;
    private ScheduledThreadPoolExecutor workers;

    public void discover(Collection<?> beans, Set<String> names) {
        var profiles = new ProfileSelection(names);
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object bean : beans) {
            if (bean == null || !seen.add(bean)) {
                continue;
            }
            for (Method method : bean.getClass().getMethods()) {
                Scheduled schedule = method.getAnnotation(Scheduled.class);
                if (schedule == null || method.isBridge() || !profiles.matches(schedule.profile())) {
                    continue;
                }
                if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                        || method.getReturnType() != void.class || !method.trySetAccessible()) {
                    throw new IllegalArgumentException("Invalid scheduled method " + method);
                }
                Runnable job = guarded(method.toString(), named(schedule.threadName(), () -> invoke(bean, method)));
                if (schedule.cron().isBlank()) {
                    interval(job, schedule.initialDelaySeconds(), schedule.intervalSeconds());
                } else {
                    if (schedule.intervalSeconds() != 0 || schedule.initialDelaySeconds() != 0) {
                        throw new IllegalArgumentException("Conflicting cron and interval settings on " + method);
                    }
                    var cron = new CronSchedule(schedule.cron(), schedule.zone());
                    cron.nextAfter(Instant.now());
                    plans.add(pool -> scheduleNext(pool, cron, job));
                }
                taskNames.add(schedule.threadName().isBlank()
                        ? bean.getClass().getSimpleName() + "#" + method.getName()
                        : schedule.threadName());
            }
        }
    }

    private static void invoke(Object bean, Method method) {
        try {
            method.invoke(bean);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(exception.getCause());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static Runnable named(String name, Runnable job) {
        if (name.isBlank()) {
            return job;
        }
        return () -> {
            Thread worker = Thread.currentThread();
            String previous = worker.getName();
            try {
                worker.setName(name);
                job.run();
            } finally {
                worker.setName(previous);
            }
        };
    }

    private Runnable guarded(String name, Runnable job) {
        return () -> {
            try {
                job.run();
            } catch (RuntimeException exception) {
                if (isErrorEnabled()) {
                    error(exception, "Scheduled job failed: ", name);
                }
            }
        };
    }

    /**
     * Registers a configured maintenance interval before startup.
     */
    public void registerInterval(String name, Runnable job, int seconds) {
        interval(guarded(name, named(name, job)), seconds, seconds);
        taskNames.add(name);
    }

    void interval(Runnable job, long delay, long interval) {
        if (delay < 0 || interval <= 0) {
            throw new IllegalArgumentException("Expected nonnegative initial delay and positive interval");
        }
        plans.add(pool -> pool.scheduleWithFixedDelay(job, delay, interval, TimeUnit.SECONDS));
    }

    private static void scheduleNext(ScheduledThreadPoolExecutor pool, CronSchedule cron, Runnable job) {
        Instant next = cron.nextAfter(Instant.now());
        if (!pool.isShutdown()) {
            pool.schedule(() -> {
                job.run();
                scheduleNext(pool, cron, job);
            }, Math.max(0, next.toEpochMilli() - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
        }
    }

    String taskListing() {
        var entries = new StringBuilder("[\n");
        if (taskNames.isEmpty()) {
            entries.append("<empty>\n");
        } else {
            taskNames.stream().sorted().forEach(name -> entries.append("  - ").append(name).append("\n"));
        }
        return entries.append("]").toString();
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        if (isInfoEnabled()) {
            info("SERVER SCHEDULED TASKS", taskListing());
        }
        if (!plans.isEmpty()) {
            workers = new ScheduledThreadPoolExecutor(2, Thread.ofPlatform().name("tenio-scheduled-", 0).factory());
            workers.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            plans.forEach(plan -> plan.accept(workers));
        }
    }

    public void shutdown() {
        if (workers != null) {
            workers.shutdownNow();
            workers = null;
        }
        plans.clear();
        taskNames.clear();
        started = false;
    }
}
