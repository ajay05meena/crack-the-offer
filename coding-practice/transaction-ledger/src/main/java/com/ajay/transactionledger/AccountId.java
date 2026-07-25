package com.ajay.transactionledger;

import java.util.Objects;

public class AccountId {
    private final Integer id;

    public AccountId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountId)) return false;
        return id.equals(((AccountId) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AccountId{" + id + '}';
    }
}
