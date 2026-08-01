package com.ajay.transactionledger;

/**
 * fromAccountId is null for DEPOSIT, toAccountId is null for WITHDRAW.
 */
public record Transaction(
        int id,
        TransactionType type,
        Integer fromAccountId,
        Integer toAccountId,
        long amount,
        long timestamp
) {
}
