package com.ajay.circuitbreaker;

public interface CircuitBreaker {
    boolean allowRequest();
    void recordSuccess();
    void recordFailure();
}
