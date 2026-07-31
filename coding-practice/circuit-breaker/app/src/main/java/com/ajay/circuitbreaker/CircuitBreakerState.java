package com.ajay.circuitbreaker;

public interface CircuitBreakerState {
    boolean allowRequest();
    void recordSuccess();
    void recordFailure();
}
