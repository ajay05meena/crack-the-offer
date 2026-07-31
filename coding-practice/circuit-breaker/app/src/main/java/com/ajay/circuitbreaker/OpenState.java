package com.ajay.circuitbreaker;

class OpenState implements CircuitBreakerState {
    private final SimpleCircuitBreaker circuitBreaker;
    private final long opentime;

    OpenState(SimpleCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
        this.opentime = System.currentTimeMillis();
    }

    @Override
    public boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (now - opentime > circuitBreaker.getResetTimeoutMillis()) {
            return circuitBreaker.transitionTo(this, new HalfOpenState(circuitBreaker));
        }
        return false;
    }

    @Override
    public void recordSuccess() {
    }

    @Override
    public void recordFailure() {
    }
}
