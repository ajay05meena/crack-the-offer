package com.ajay.ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeBucketedRateLimiterTest {

    // The implementation buckets on System.currentTimeMillis() / 6000 with no injectable
    // clock, so tests can't control the window directly. This just avoids starting a test
    // right at a bucket boundary, where a call could land in the wrong bucket.
    private static final long BUCKET_MILLIS = 6000L;

    private static void waitUntilFreshBucket() throws InterruptedException {
        while (System.currentTimeMillis() % BUCKET_MILLIS > 500) {
            Thread.sleep(50);
        }
    }

    @Test
    public void callsAtOrUnderThresholdReturnFalse() throws InterruptedException {
        // NOTE: tryAcquire returns `currentCalls > maxAllowedRequest`, the opposite of what
        // the RateLimiter contract implies (true == allowed). This documents the actual
        // current behavior rather than the presumably intended one.
        waitUntilFreshBucket();
        TimeBucketedRateLimiter limiter = new TimeBucketedRateLimiter(3L);

        Assertions.assertFalse(limiter.tryAcquire("user1"), "1st call (count=1) is at/under threshold -> false");
        Assertions.assertFalse(limiter.tryAcquire("user1"), "2nd call (count=2) is at/under threshold -> false");
        Assertions.assertFalse(limiter.tryAcquire("user1"), "3rd call (count=3) is at/under threshold -> false");
        Assertions.assertTrue(limiter.tryAcquire("user1"), "4th call (count=4) exceeds threshold -> true");
    }

    @Test
    public void tracksEachUserIndependentlyWithinSameBucket() throws InterruptedException {
        waitUntilFreshBucket();
        TimeBucketedRateLimiter limiter = new TimeBucketedRateLimiter(1L);

        Assertions.assertFalse(limiter.tryAcquire("alice"), "alice's 1st call, count=1, at threshold -> false");
        Assertions.assertFalse(limiter.tryAcquire("bob"), "bob's count is tracked separately from alice's");
    }

    @Test
    public void countResetsOnceTimeBucketRolls() throws InterruptedException {
        waitUntilFreshBucket();
        TimeBucketedRateLimiter limiter = new TimeBucketedRateLimiter(1L);

        Assertions.assertFalse(limiter.tryAcquire("user1"), "1st call in this bucket, count=1 -> false");
        Assertions.assertTrue(limiter.tryAcquire("user1"), "2nd call in same bucket, count=2 -> true");

        Thread.sleep(BUCKET_MILLIS + 100); // let the wall-clock bucket roll over

        Assertions.assertFalse(limiter.tryAcquire("user1"), "new bucket resets the per-user count back to 1 -> false");
    }
}
