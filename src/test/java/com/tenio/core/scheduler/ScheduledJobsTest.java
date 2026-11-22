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

import com.tenio.core.bootstrap.annotation.Scheduled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduledJobsTest {

    @Test
    void optionalThreadNameIsScopedToOneInvocation() throws Exception {
        String original = Thread.currentThread().getName();
        String omitted = (String) Scheduled.class.getMethod("threadName").getDefaultValue();
        ScheduledJobs.named(omitted, () -> assertEquals(original, Thread.currentThread().getName())).run();
        Runnable failing = ScheduledJobs.named("ccu-sample", () -> {
            assertEquals("ccu-sample", Thread.currentThread().getName());
            throw new IllegalStateException();
        });
        assertThrows(IllegalStateException.class, failing::run);
        assertEquals(original, Thread.currentThread().getName());
    }

    @Test
    void listsOnlyActiveJobsInNameOrderAndClearsOnShutdown() {
        var jobs = new ScheduledJobs();
        jobs.discover(List.of(new Reporter(), new Once()), Set.of("metrics"));
        assertEquals("[\n  - Once#run\n  - REPORT_TEST_METRICS\n]", jobs.taskListing());
        jobs.shutdown();
        assertEquals("[\n<empty>\n]", jobs.taskListing());
    }

    static class Reporter {

        final AtomicInteger attempts = new AtomicInteger();
        final AtomicInteger inactiveCalls = new AtomicInteger();
        final CountDownLatch recovered = new CountDownLatch(1);

        @Scheduled(threadName = "REPORT_TEST_METRICS", intervalSeconds = 1, profile = "metrics")
        public void report() {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("First invocation failed");
            }
            recovered.countDown();
        }

        @Scheduled(intervalSeconds = 1, profile = "game")
        public void inactive() {
            inactiveCalls.incrementAndGet();
        }
    }

    @Test
    void activeJobContinuesAfterFailureAndInactiveJobIsSkipped() throws InterruptedException {
        var jobs = new ScheduledJobs();
        var reporter = new Reporter();
        jobs.discover(List.of(reporter), Set.of("world", "metrics"));
        try {
            jobs.start();
            assertTrue(reporter.recovered.await(5, TimeUnit.SECONDS));
        } finally {
            jobs.shutdown();
        }
        assertEquals(0, reporter.inactiveCalls.get());
    }

    static class Once {
        final CountDownLatch first = new CountDownLatch(1);
        final CountDownLatch duplicate = new CountDownLatch(1);
        final AtomicInteger calls = new AtomicInteger();

        @Scheduled(intervalSeconds = 60)
        public void run() {
            if (calls.incrementAndGet() > 1) {
                duplicate.countDown();
            }
            first.countDown();
        }
    }

    @Test
    void beanAliasesAndRepeatedStartRegisterOneCallback() throws InterruptedException {
        var bean = new Once();
        var jobs = new ScheduledJobs();
        jobs.discover(List.of(bean, bean), Set.of());
        try {
            jobs.start();
            jobs.start();
            assertTrue(bean.first.await(5, TimeUnit.SECONDS));
            assertFalse(bean.duplicate.await(250, TimeUnit.MILLISECONDS));
        } finally {
            jobs.shutdown();
        }
    }

    @Test
    void rejectsMixedTriggersAndInvalidMethodsBeforeStartingWorkers() {
        var jobs = new ScheduledJobs();
        var mixed = new Object() {
            @Scheduled(intervalSeconds = 1, cron = "* * * * *")
            public void run() {
            }
        };
        var invalid = new Object() {
            @Scheduled(intervalSeconds = 1)
            public void run(int parameter) {
            }
        };
        assertThrows(IllegalArgumentException.class, () -> jobs.discover(List.of(mixed), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> jobs.discover(List.of(invalid), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> jobs.interval(() -> {
        }, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> jobs.interval(() -> {
        }, 0, 0));
    }
}
