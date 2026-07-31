package com.ajay.circuitbreaker;

import java.util.concurrent.atomic.AtomicReference;

public class SimpleCircuitBreaker implements CircuitBreaker {

    private final AtomicReference<CircuitBreakerState> state;
    private final int failureThreshold;
    private final long resetTimeoutMillis;

    public SimpleCircuitBreaker(int failureThreshold, long resetTimeoutMillis) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
        this.state = new AtomicReference<>(new ClosedState(this));
    }

    public boolean transitionTo(CircuitBreakerState oldState, CircuitBreakerState newState) {
        return state.compareAndSet(oldState, newState);
    }
    @Override
    public boolean allowRequest() {
        return state.get().allowRequest();
    }

    @Override
    public void recordSuccess() {
        state.get().recordSuccess();
    }

    @Override
    public void recordFailure() {
        state.get().recordFailure();
    }

    int getFailureThreshold() {
        return failureThreshold;
    }

    long getResetTimeoutMillis() {
        return resetTimeoutMillis;
    }
}
