package com.ajay.transactionledger;

import java.math.BigDecimal;
import java.util.Objects;

public final class Amount {
    private final BigDecimal value;

    public Amount(double amount) {
        this(toBigDecimal(amount));
    }

    public Amount(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.value = amount;
    }

    private static BigDecimal toBigDecimal(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("amount must be a finite number");
        }
        return BigDecimal.valueOf(amount);
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Amount)) return false;
        return value.compareTo(((Amount) o).value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return "Amount{" + value + '}';
    }
}
