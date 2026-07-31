package com.ajay.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter{
    private final long maxCapacity;
    private final double refillRatePerMinuites;
    private  final Map<String, Bucket> userBuckets;

    public TokenBucketRateLimiter(long maxCapacity, double refillRatePerMinutes) {
        this.maxCapacity = maxCapacity;
        this.refillRatePerMinuites = refillRatePerMinutes;
        this.userBuckets = new ConcurrentHashMap<>();
    }

    private static class Bucket{
        double tokens;
        long lastRefillTimeStamp;

        Bucket(double tokens, long lastRefillTimeStamp){
            this.tokens = tokens;
            this.lastRefillTimeStamp = lastRefillTimeStamp;
        }
    }
    @Override
    public boolean tryAcquire(String userId) {
        long now = System.currentTimeMillis();
        boolean [] isAllowed = new boolean[1];

        userBuckets.compute(userId, (key, bucket) -> {

                    if (bucket == null) {
                        isAllowed[0] = true;
                        return new Bucket(maxCapacity - 1, now);
                    }
                    long timeElapsed = now - bucket.lastRefillTimeStamp;
                    double tokensToAdd = (timeElapsed / 60000.0) * refillRatePerMinuites;
                    bucket.tokens = Math.min(maxCapacity, bucket.tokens + tokensToAdd);
                    bucket.lastRefillTimeStamp = now;
                    if (bucket.tokens >= 1.0) {
                        bucket.tokens = bucket.tokens - 1;
                        isAllowed[0] = true;
                    }
                    return bucket;
                }
            );
        return isAllowed[0];
    }
}
