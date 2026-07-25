package com.ajay.transactionledger.impl;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Balance for a single account, guarded by {@link #lock}. {@code credit}/{@code tryDebit}
 * assume the caller already holds the lock; {@link #balance()} reads the volatile field
 * directly so callers can read the balance without contending for the lock.
 */
final class Account {
    private static final AtomicLong LOCK_ORDER_GENERATOR = new AtomicLong();

    // fixed per-account ordering so multi-account operations (transfer) can acquire
    // both locks in a consistent order and never deadlock against each other
    final long lockOrder = LOCK_ORDER_GENERATOR.incrementAndGet();
    final ReentrantLock lock = new ReentrantLock();

    private volatile BigDecimal balance = BigDecimal.ZERO;

    BigDecimal balance() {
        return balance;
    }

    void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    boolean tryDebit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            return false;
        }
        balance = balance.subtract(amount);
        return true;
    }
}
