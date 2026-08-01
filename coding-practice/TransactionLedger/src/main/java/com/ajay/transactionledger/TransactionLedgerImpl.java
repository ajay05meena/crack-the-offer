package com.ajay.transactionledger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class TransactionLedgerImpl implements TransactionLedger {

    private final Map<Integer, Long> balances = new ConcurrentHashMap<>();
    private final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<Integer, List<Integer>> userToTransactionIds = new ConcurrentHashMap<>();
    private final List<Transaction> sortedTransactions = new CopyOnWriteArrayList<>();
    // ponytail: single lock serializes sorted-list inserts (find-index + insert must be atomic
    // together); shard by time bucket if insert throughput ever becomes the bottleneck.
    private final Object sortedInsertLock = new Object();
    private final AtomicInteger nextTransactionId = new AtomicInteger();

    @Override
    public void deposit(int accountId, long amount) {
        validateAmount(amount);
        credit(accountId, amount);
        record(TransactionType.DEPOSIT, null, accountId, amount);
    }

    @Override
    public void withdraw(int accountId, long amount) {
        validateAmount(amount);
        debit(accountId, amount);
        record(TransactionType.WITHDRAW, accountId, null, amount);
    }

    @Override
    public void transfer(int fromAccountId, int toAccountId, long amount) {
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("cannot transfer to the same account");
        }
        validateAmount(amount);
        debit(fromAccountId, amount);
        credit(toAccountId, amount);
        record(TransactionType.TRANSFER, fromAccountId, toAccountId, amount);
    }

    @Override
    public long getBalance(int accountId) {
        return balances.getOrDefault(accountId, 0L);
    }

    @Override
    public List<Transaction> getTransactions(int accountId) {
        List<Integer> ids = userToTransactionIds.getOrDefault(accountId, List.of());
        List<Transaction> result = new ArrayList<>(ids.size());
        for (int id : ids) {
            result.add(transactions.get(id));
        }
        return result;
    }

    @Override
    public List<Transaction> getTransactionsByTimestampRange(long fromTimestamp, long toTimestamp) {
        if (fromTimestamp > toTimestamp) {
            throw new IllegalArgumentException("fromTimestamp must be <= toTimestamp");
        }
        List<Transaction> snapshot = new ArrayList<>(sortedTransactions);
        int start = lowerBound(snapshot, fromTimestamp);
        int end = upperBound(snapshot, toTimestamp);
        return List.copyOf(snapshot.subList(start, end));
    }

    // Atomic check-then-act: ConcurrentHashMap.compute() holds the bin lock for accountId for
    // the whole remapping function, so the balance check and debit can't race with another
    // debit/credit on the same account. Throwing inside the function leaves the map unchanged.
    private void debit(int accountId, long amount) {
        balances.compute(accountId, (id, balance) -> {
            long current = balance == null ? 0L : balance;
            if (current < amount) {
                throw new IllegalStateException("insufficient funds for account " + accountId);
            }
            return current - amount;
        });
    }

    private void credit(int accountId, long amount) {
        balances.merge(accountId, amount, Long::sum);
    }

    private void record(TransactionType type, Integer fromAccountId, Integer toAccountId, long amount) {
        int id = nextTransactionId.getAndIncrement();
        Transaction transaction = new Transaction(id, type, fromAccountId, toAccountId, amount, System.currentTimeMillis());
        transactions.put(id, transaction);
        if (fromAccountId != null) {
            userToTransactionIds.computeIfAbsent(fromAccountId, k -> new CopyOnWriteArrayList<>()).add(id);
        }
        if (toAccountId != null) {
            userToTransactionIds.computeIfAbsent(toAccountId, k -> new CopyOnWriteArrayList<>()).add(id);
        }
        synchronized (sortedInsertLock) {
            int index = lowerBound(sortedTransactions, transaction.timestamp());
            sortedTransactions.add(index, transaction);
        }
    }

    private static int lowerBound(List<Transaction> list, long timestamp) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (list.get(mid).timestamp() < timestamp) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    // Exclusive end index for an inclusive-of-timestamp range; avoids overflow from timestamp + 1.
    private static int upperBound(List<Transaction> list, long timestamp) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (list.get(mid).timestamp() <= timestamp) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    private static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
