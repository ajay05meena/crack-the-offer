package com.ajay.transactionledger.impl;

import com.ajay.transactionledger.AccountId;
import com.ajay.transactionledger.Amount;
import com.ajay.transactionledger.Transaction;
import com.ajay.transactionledger.TransactionLedger;
import com.ajay.transactionledger.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class TransactionLedgerImpl implements TransactionLedger {

    private final ConcurrentHashMap<AccountId, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<AccountId, TransactionStore> transactionsByAccount = new ConcurrentHashMap<>();
    private final TransactionStore allTransactions = new TransactionStore();

    @Override
    public boolean deposit(AccountId accountId, Amount amount) {
        validateAccount(accountId);
        validateAmount(amount);

        Account account = accountFor(accountId);
        account.lock.lock();
        try {
            account.credit(amount.value());
        } finally {
            account.lock.unlock();
        }

        Transaction transaction = new Transaction(null, accountId, TransactionType.DEPOSIT, amount, System.currentTimeMillis());
        record(transaction, accountId);
        return true;
    }

    @Override
    public boolean withdraw(AccountId accountId, Amount amount) {
        validateAccount(accountId);
        validateAmount(amount);

        Account account = accountFor(accountId);
        boolean debited;
        account.lock.lock();
        try {
            debited = account.tryDebit(amount.value());
        } finally {
            account.lock.unlock();
        }
        if (!debited) {
            return false;
        }

        Transaction transaction = new Transaction(accountId, null, TransactionType.WITHDRAW, amount, System.currentTimeMillis());
        record(transaction, accountId);
        return true;
    }

    @Override
    public boolean transfer(AccountId from, AccountId to, Amount amount) {
        validateAccount(from);
        validateAccount(to);
        validateAmount(amount);
        if (from.equals(to)) {
            throw new IllegalArgumentException("from and to accounts must be different");
        }

        Account fromAccount = accountFor(from);
        Account toAccount = accountFor(to);
        boolean debited = lockBothAndApply(fromAccount, toAccount, () -> {
            if (!fromAccount.tryDebit(amount.value())) {
                return false;
            }
            toAccount.credit(amount.value());
            return true;
        });
        if (!debited) {
            return false;
        }

        Transaction transaction = new Transaction(from, to, TransactionType.TRANSFER, amount, System.currentTimeMillis());
        record(transaction, from, to);
        return true;
    }

    @Override
    public BigDecimal getBalance(AccountId accountId) {
        validateAccount(accountId);
        Account account = accounts.get(accountId);
        return account == null ? BigDecimal.ZERO : account.balance();
    }

    @Override
    public List<Transaction> getTransactions(AccountId accountId) {
        validateAccount(accountId);
        TransactionStore store = transactionsByAccount.get(accountId);
        return store == null ? List.of() : store.all();
    }

    @Override
    public List<Transaction> getTransactionsByTimeStampRange(Long fromTimeStamp, Long toTimeStamp) {
        if (fromTimeStamp == null || toTimeStamp == null) {
            throw new IllegalArgumentException("timestamps must not be null");
        }
        if (fromTimeStamp > toTimeStamp) {
            throw new IllegalArgumentException("fromTimeStamp must not be after toTimeStamp");
        }
        return allTransactions.range(fromTimeStamp, toTimeStamp);
    }

    private Account accountFor(AccountId accountId) {
        return accounts.computeIfAbsent(accountId, id -> new Account());
    }

    // Acquires both accounts' locks in a fixed, account-independent order so that two
    // transfers moving money in opposite directions can never deadlock against each other.
    private boolean lockBothAndApply(Account a, Account b, java.util.function.BooleanSupplier action) {
        Account first = a.lockOrder < b.lockOrder ? a : b;
        Account second = first == a ? b : a;
        first.lock.lock();
        try {
            second.lock.lock();
            try {
                return action.getAsBoolean();
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }

    private void record(Transaction transaction, AccountId... accountIds) {
        for (AccountId accountId : accountIds) {
            transactionsByAccount.computeIfAbsent(accountId, id -> new TransactionStore()).add(transaction);
        }
        allTransactions.add(transaction);
    }

    private void validateAccount(AccountId accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
    }

    private void validateAmount(Amount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
    }
}
