package com.ajay.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SlidingWindowRateLimiterTest {

    @Test
    public void allowsUpToThresholdRequestsWithinWindow() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3L, 1000L);

        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertFalse(limiter.tryAcquire("user1"), "4th request within the window should be rejected");
    }

    @Test
    public void allowsRequestAgainOnceOldestEntrySlidesOutOfWindow() throws InterruptedException {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1L, 200L);

        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertFalse(limiter.tryAcquire("user1"), "still inside the window");

        Thread.sleep(250);

        Assertions.assertTrue(limiter.tryAcquire("user1"), "oldest entry has slid out of the window");
    }

    @Test
    public void tracksEachUserIndependently() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1L, 1000L);

        Assertions.assertTrue(limiter.tryAcquire("alice"));
        Assertions.assertFalse(limiter.tryAcquire("alice"), "alice already used her request this window");
        Assertions.assertTrue(limiter.tryAcquire("bob"), "bob's quota is independent of alice's");
    }
}
