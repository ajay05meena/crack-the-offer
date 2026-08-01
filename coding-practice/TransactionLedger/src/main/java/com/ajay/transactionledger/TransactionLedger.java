package com.ajay.transactionledger;

import java.util.List;

public interface TransactionLedger {
    void deposit(int accountId, long amount);

    void withdraw(int accountId, long amount);

    void transfer(int fromAccountId, int toAccountId, long amount);

    long getBalance(int accountId);

    List<Transaction> getTransactions(int accountId);

    List<Transaction> getTransactionsByTimestampRange(long fromTimestamp, long toTimestamp);
}
