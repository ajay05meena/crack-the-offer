package com.ajay.ratelimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Thread-safe and lock-free: {@link #tryAcquire()} may be called concurrently from
 * multiple threads without blocking. The window-start and count are read together and
 * updated via a single {@code compareAndSet} on one immutable {@link WindowState}, so a
 * window transition and a count update can never be observed or applied separately —
 * there's no in-between state for a second thread to race against.
 */
public class FixedWindowRateLimiter implements RateLimiter{

    private record WindowState(long windowStartNanos, int count) {
    }

    private final int threshold;
    private final long windowNanos;
    private final LongSupplier nanoClock;
    private final AtomicReference<WindowState> state;

    public FixedWindowRateLimiter(int threshold) {
        this(threshold, 1000L);
    }

    public FixedWindowRateLimiter(int threshold, long windowMillis) {
        this(threshold, TimeUnit.MILLISECONDS.toNanos(windowMillis), System::nanoTime);
    }

    // Package-private: lets tests inject a fake clock for deterministic window-boundary testing,
    // instead of relying on real Thread.sleep. Takes nanoseconds and defaults to System::nanoTime,
    // not System.currentTimeMillis()
    FixedWindowRateLimiter(int threshold, long windowNanos, LongSupplier nanoClock) {
        this.threshold = threshold;
        this.windowNanos = windowNanos;
        this.nanoClock = nanoClock;
        this.state = new AtomicReference<>(new WindowState(nanoClock.getAsLong(), 0));
    }

    // CAS retry loop, not a lock: under very high contention threads spin and retry
    // rather than block/sleep — cheaper than a monitor for this short a critical section,
    // but a lever worth revisiting if contention on a single key ever gets extreme.
    @Override
    public boolean tryAcquire() {
        while (true) {
            WindowState current = state.get();
            long now = nanoClock.getAsLong();
            WindowState next = (now - current.windowStartNanos() >= windowNanos)
                    ? new WindowState(now, 1)
                    : new WindowState(current.windowStartNanos(), current.count() + 1);

            if (state.compareAndSet(current, next)) {
                return next.count() <= threshold;
            }
            // Another thread updated state concurrently — retry with the fresh value.
        }
    }
}
