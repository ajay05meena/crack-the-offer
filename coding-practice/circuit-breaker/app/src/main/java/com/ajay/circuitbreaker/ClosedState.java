package com.ajay.circuitbreaker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class ClosedState implements CircuitBreakerState {
    private final SimpleCircuitBreaker circuitBreaker;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicBoolean opened = new AtomicBoolean();

    ClosedState(SimpleCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public boolean allowRequest() {
        return true; //System is healthy, let request go through
    }

    @Override
    public void recordSuccess() {
        failureCount.set(0);
    }

    @Override
    public void recordFailure() {
        if(failureCount.incrementAndGet() >= circuitBreaker.getFailureThreshold() && opened.compareAndSet(false, true)){
            circuitBreaker.transitionTo(this, new OpenState(circuitBreaker));
        }
    }
}
