package com.ajay.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucketRateLimiter implements RateLimiter {
    private final long capacity;
    private final double leakRatePerMinute;
    private final Map<String, Bucket> userBuckets;

    public LeakyBucketRateLimiter(long capacity, double leakRatePerMinute) {
        this.capacity = capacity;
        this.leakRatePerMinute = leakRatePerMinute;
        this.userBuckets = new ConcurrentHashMap<>();
    }

    private static class Bucket {
        double level;
        long lastLeakTimestamp;

        Bucket(double level, long lastLeakTimestamp) {
            this.level = level;
            this.lastLeakTimestamp = lastLeakTimestamp;
        }
    }

    @Override
    public boolean tryAcquire(String userId) {
        long now = System.currentTimeMillis();
        boolean[] isAllowed = new boolean[1];

        userBuckets.compute(userId, (key, bucket) -> {
            if (bucket == null) {
                isAllowed[0] = true;
                return new Bucket(1, now);
            }
            long timeElapsed = now - bucket.lastLeakTimestamp;
            double leaked = (timeElapsed / 60000.0) * leakRatePerMinute;
            bucket.level = Math.max(0, bucket.level - leaked);
            bucket.lastLeakTimestamp = now;
            if (bucket.level + 1 <= capacity) {
                bucket.level += 1;
                isAllowed[0] = true;
            }
            return bucket;
        });
        return isAllowed[0];
    }
}
