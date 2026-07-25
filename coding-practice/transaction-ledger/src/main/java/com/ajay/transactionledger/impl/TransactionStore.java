package com.ajay.transactionledger.impl;

import com.ajay.transactionledger.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Transactions ordered by timestamp. Backed by a skip list keyed on timestamp, so inserts
 * and range queries are O(log n) with no manual locking, and concurrent inserts at the same
 * timestamp are appended (not reordered) within that timestamp's bucket.
 */
final class TransactionStore {
    private final ConcurrentSkipListMap<Long, ConcurrentLinkedQueue<Transaction>> byTimestamp =
            new ConcurrentSkipListMap<>();

    void add(Transaction transaction) {
        byTimestamp.computeIfAbsent(transaction.getTimestamp(), t -> new ConcurrentLinkedQueue<>())
                .add(transaction);
    }

    List<Transaction> all() {
        List<Transaction> result = new ArrayList<>();
        for (ConcurrentLinkedQueue<Transaction> bucket : byTimestamp.values()) {
            result.addAll(bucket);
        }
        return result;
    }

    List<Transaction> range(long fromTimestamp, long toTimestamp) {
        List<Transaction> result = new ArrayList<>();
        for (Map.Entry<Long, ConcurrentLinkedQueue<Transaction>> entry :
                byTimestamp.subMap(fromTimestamp, true, toTimestamp, true).entrySet()) {
            result.addAll(entry.getValue());
        }
        return result;
    }
}
