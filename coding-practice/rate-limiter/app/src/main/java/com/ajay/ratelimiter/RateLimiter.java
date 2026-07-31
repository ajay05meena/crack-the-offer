package com.ajay.ratelimiter;

public interface RateLimiter {
    boolean tryAcquire(String userId);
}
