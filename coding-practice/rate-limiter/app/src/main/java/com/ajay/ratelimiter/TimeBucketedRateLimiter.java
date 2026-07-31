package com.ajay.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TimeBucketedRateLimiter implements RateLimiter{

    private final Map<Long, Map<String, Long>> timeBuckets;
    private final Long maxAllowedRequest;

    public TimeBucketedRateLimiter(Long maxAllowedRequest) {
        this.timeBuckets = new ConcurrentHashMap<>();
        this.maxAllowedRequest = maxAllowedRequest;
    }

    @Override
    public boolean tryAcquire(String userId) {
       long currentMinute = System.currentTimeMillis() / 6000;
       timeBuckets.putIfAbsent(currentMinute, new ConcurrentHashMap<>());
       Long currentCalls = timeBuckets.get(currentMinute).merge(userId, 1L, Long::sum);
       return currentCalls > maxAllowedRequest;
    }
}
