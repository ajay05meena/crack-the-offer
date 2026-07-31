package com.ajay.circuitbreaker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleCircuitBreakerTest {

    @Test
    void allowsRequestsWhenClosed() {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 100);
        assertTrue(cb.allowRequest());
    }

    @Test
    void successResetsFailureCount() {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 100);
        cb.recordFailure();
        cb.recordFailure();
        cb.recordSuccess();
        cb.recordFailure();
        cb.recordFailure();

        assertTrue(cb.allowRequest()); // still closed, count was reset after success
    }

    @Test
    void opensAfterThresholdConsecutiveFailures() {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 10_000);
        cb.recordFailure();
        cb.recordFailure();
        cb.recordFailure();

        assertFalse(cb.allowRequest());
    }

    @Test
    void allowsSingleProbeAfterResetTimeoutThenClosesOnSuccess() throws InterruptedException {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(1, 100);
        cb.recordFailure(); // opens
        Thread.sleep(150);

        assertTrue(cb.allowRequest()); // the probe
        cb.recordSuccess();

        assertTrue(cb.allowRequest()); // closed again
    }

    @Test
    void reopensWhenProbeFails() throws InterruptedException {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(1, 100);
        cb.recordFailure(); // opens
        Thread.sleep(150);

        assertTrue(cb.allowRequest()); // the probe
        cb.recordFailure();

        assertFalse(cb.allowRequest()); // back to open, immediate reject
    }

    @Test
    void onlyOneThreadIsAllowedThroughAsProbeAfterResetTimeout() throws Exception {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(1, 100);
        cb.recordFailure(); // opens
        Thread.sleep(150);

        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowedCount = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                await(start);
                if (cb.allowRequest()) {
                    allowedCount.incrementAndGet();
                }
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(1, allowedCount.get());
    }

    @Test
    void concurrentFailuresOpenCircuitWithoutError() throws Exception {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(5, 10_000);
        int threadCount = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                await(start);
                cb.recordFailure();
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertFalse(cb.allowRequest());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
