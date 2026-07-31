package com.ajay.circuitbreaker;

import java.util.concurrent.atomic.AtomicBoolean;

class HalfOpenState implements CircuitBreakerState {
    private final SimpleCircuitBreaker circuitBreaker;
    private final AtomicBoolean isTesting = new AtomicBoolean();

    HalfOpenState(SimpleCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public boolean allowRequest() {
        return isTesting.compareAndSet(false, true);
    }

    @Override
    public void recordSuccess() {
        circuitBreaker.transitionTo(this, new ClosedState(circuitBreaker));
    }

    @Override
    public void recordFailure() {
        circuitBreaker.transitionTo(this, new OpenState(circuitBreaker));
    }
}
