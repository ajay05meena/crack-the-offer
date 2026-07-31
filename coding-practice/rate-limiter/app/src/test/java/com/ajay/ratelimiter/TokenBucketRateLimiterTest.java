package com.ajay.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenBucketRateLimiterTest {

    @Test
    public void allowsExactlyCapacityRequestsWithNoRefill() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 0.0);

        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertFalse(limiter.tryAcquire("user1"), "bucket is empty, 4th request is rejected");
    }

    @Test
    public void tracksEachUserIndependently() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.0);

        Assertions.assertTrue(limiter.tryAcquire("alice"));
        Assertions.assertFalse(limiter.tryAcquire("alice"), "alice's bucket is already empty");
        Assertions.assertTrue(limiter.tryAcquire("bob"), "bob's bucket is independent of alice's");
    }

    @Test
    public void refillRateIsAppliedPerMinuteNotPerMillisecond() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0);

        Assertions.assertTrue(limiter.tryAcquire("user1"), "1st request seeds the bucket -> allowed");
        Assertions.assertTrue(limiter.tryAcquire("user1"), "2nd request empties the bucket -> allowed");
        Assertions.assertFalse(limiter.tryAcquire("user1"), "bucket is empty -> rejected");

        Thread.sleep(5);

        Assertions.assertFalse(limiter.tryAcquire("user1"),
                "a 5ms delay at a nominal 1 token/minute rate barely refills anything, so it stays rejected");
    }
}
