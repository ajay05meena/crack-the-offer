package com.ajay.transactionledger;

import java.math.BigDecimal;
import java.util.List;

/**
 * A thread-safe ledger of account balances and the transactions that produced them.
 * Implementations must support concurrent calls from multiple threads for any account,
 * including concurrent operations that touch the same account(s).
 */
public interface TransactionLedger {

    /**
     * Credits {@code amount} to {@code accountId}. Always succeeds for a valid account/amount;
     * returns {@code true} for interface symmetry with {@link #withdraw} and {@link #transfer}.
     *
     * @throws IllegalArgumentException if accountId or amount is null
     */
    boolean deposit(AccountId accountId, Amount amount);

    /**
     * Debits {@code amount} from {@code accountId}.
     *
     * @return {@code true} if the account had sufficient balance and was debited,
     *         {@code false} if the balance was insufficient (no state is changed)
     * @throws IllegalArgumentException if accountId or amount is null
     */
    boolean withdraw(AccountId accountId, Amount amount);

    /**
     * Atomically moves {@code amount} from {@code from} to {@code to}.
     *
     * @return {@code true} if {@code from} had sufficient balance and the transfer completed,
     *         {@code false} if the balance was insufficient (no state is changed)
     * @throws IllegalArgumentException if either account or the amount is null, or from equals to
     */
    boolean transfer(AccountId from, AccountId to, Amount amount);

    /**
     * @return the current balance of {@code accountId}, or zero if the account has never been touched
     */
    BigDecimal getBalance(AccountId accountId);

    /**
     * @return this account's transactions in ascending timestamp order
     */
    List<Transaction> getTransactions(AccountId accountId);

    /**
     * @return all transactions with a timestamp in {@code [fromTimeStamp, toTimeStamp]}, ascending order
     */
    List<Transaction> getTransactionsByTimeStampRange(Long fromTimeStamp, Long toTimeStamp);
}
