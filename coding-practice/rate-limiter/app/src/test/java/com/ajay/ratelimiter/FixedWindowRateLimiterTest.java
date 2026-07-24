package com.ajay.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FixedWindowRateLimiterTest {

    private static final long ONE_SECOND_NANOS = TimeUnit.SECONDS.toNanos(1);

    @Test
    public void allowsUpToThresholdRequestsWithinWindow() {
        AtomicLong fakeClock = new AtomicLong(0L);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(3, ONE_SECOND_NANOS, fakeClock::get);

        Assertions.assertTrue(limiter.tryAcquire(), "1st request within threshold should be allowed");
        Assertions.assertTrue(limiter.tryAcquire(), "2nd request within threshold should be allowed");
        Assertions.assertTrue(limiter.tryAcquire(), "3rd request within threshold should be allowed");
        Assertions.assertFalse(limiter.tryAcquire(), "4th request exceeds threshold and should be rejected");
    }

    @Test
    public void rejectsExactlyOneOverThreshold() {
        AtomicLong fakeClock = new AtomicLong(0L);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(1, ONE_SECOND_NANOS, fakeClock::get);

        Assertions.assertTrue(limiter.tryAcquire(), "exactly at threshold should be allowed");
        Assertions.assertFalse(limiter.tryAcquire(), "one over threshold should be rejected");
    }

    @Test
    public void doesNotResetBeforeWindowElapses() {
        AtomicLong fakeClock = new AtomicLong(0L);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(1, ONE_SECOND_NANOS, fakeClock::get);

        Assertions.assertTrue(limiter.tryAcquire());
        fakeClock.set(ONE_SECOND_NANOS - 1); // one nanosecond before the window elapses

        Assertions.assertFalse(limiter.tryAcquire(), "window should not reset before the window has elapsed");
    }

    @Test
    public void resetsCounterOnceWindowElapses() {
        AtomicLong fakeClock = new AtomicLong(0L);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, ONE_SECOND_NANOS, fakeClock::get);

        Assertions.assertTrue(limiter.tryAcquire());
        Assertions.assertTrue(limiter.tryAcquire());
        Assertions.assertFalse(limiter.tryAcquire(), "3rd request in the same window should be rejected");

        fakeClock.set(ONE_SECOND_NANOS); // exactly one window later

        Assertions.assertTrue(limiter.tryAcquire(), "new window should allow requests again");
        Assertions.assertTrue(limiter.tryAcquire());
        Assertions.assertFalse(limiter.tryAcquire(), "3rd request in the new window should be rejected too");
    }

    /**
     * Regression test for the window-reset race originally found in review: many threads
     * racing the window boundary concurrently must never admit more than `threshold`
     * requests total. Now passes by construction (single CAS on one immutable WindowState),
     * not by luck — see the stress validation noted in the implementation's class comment.
     */
    @Test
    public void neverAdmitsMoreThanThresholdUnderConcurrentLoadAtWindowBoundary() throws InterruptedException {
        int threshold = 5;
        AtomicLong fakeClock = new AtomicLong(0L);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(threshold, ONE_SECOND_NANOS, fakeClock::get);
        fakeClock.set(ONE_SECOND_NANOS); // clock now sits exactly at the window boundary for every racing thread

        int threadCount = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger admitted = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (limiter.tryAcquire()) {
                    admitted.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        Assertions.assertTrue(admitted.get() <= threshold,
                "admitted " + admitted.get() + " requests but threshold was " + threshold);
    }
}
