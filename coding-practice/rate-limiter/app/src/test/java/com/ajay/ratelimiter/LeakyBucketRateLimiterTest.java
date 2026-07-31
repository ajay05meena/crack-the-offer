package com.ajay.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LeakyBucketRateLimiterTest {

    @Test
    public void allowsExactlyCapacityRequestsWithNoLeak() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(3, 0.0);

        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertFalse(limiter.tryAcquire("user1"), "bucket is full, 4th request overflows it");
    }

    @Test
    public void tracksEachUserIndependently() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(1, 0.0);

        Assertions.assertTrue(limiter.tryAcquire("alice"));
        Assertions.assertFalse(limiter.tryAcquire("alice"), "alice's bucket is already full");
        Assertions.assertTrue(limiter.tryAcquire("bob"), "bob's bucket is independent of alice's");
    }

    @Test
    public void leakingOverTimeFreesUpRoomForMoreRequests() throws InterruptedException {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(1, 60000.0);

        Assertions.assertTrue(limiter.tryAcquire("user1"));
        Assertions.assertFalse(limiter.tryAcquire("user1"), "bucket is still full");

        Thread.sleep(5);

        Assertions.assertTrue(limiter.tryAcquire("user1"), "enough has leaked out to admit another request");
    }
}
