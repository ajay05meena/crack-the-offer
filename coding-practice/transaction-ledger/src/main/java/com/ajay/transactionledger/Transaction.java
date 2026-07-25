package com.ajay.transactionledger;

public final class Transaction {

    private final AccountId source; // null for DEPOSIT (money enters from outside the ledger)
    private final AccountId dest;   // null for WITHDRAW (money leaves the ledger)
    private final TransactionType transactionType;
    private final Amount amount;
    private final long timestamp;

    public Transaction(AccountId source, AccountId dest, TransactionType transactionType, Amount amount, long timestamp) {
        this.source = source;
        this.dest = dest;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public AccountId getSource() {
        return source;
    }

    public AccountId getDest() {
        return dest;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Amount getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + transactionType +
                ", source=" + source +
                ", dest=" + dest +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
