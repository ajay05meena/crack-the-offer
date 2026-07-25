package com.ajay.transactionledger.impl;

import org.junit.jupiter.api.Test;
import com.ajay.transactionledger.AccountId;
import com.ajay.transactionledger.Amount;
import com.ajay.transactionledger.Transaction;
import com.ajay.transactionledger.TransactionLedger;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionLedgerImplTest {

    private final TransactionLedger ledger = new TransactionLedgerImpl();

    @Test
    void depositCreditsBalanceAndRecordsTransaction() {
        AccountId a = new AccountId(1);

        assertTrue(ledger.deposit(a, new Amount(100.0)));

        assertEquals(0, new BigDecimal("100.0").compareTo(ledger.getBalance(a)));
        List<Transaction> transactions = ledger.getTransactions(a);
        assertEquals(1, transactions.size());
        assertEquals(a, transactions.get(0).getDest());
    }

    @Test
    void withdrawFailsAndLeavesBalanceUnchangedWhenInsufficientFunds() {
        AccountId a = new AccountId(1);
        ledger.deposit(a, new Amount(50.0));

        assertFalse(ledger.withdraw(a, new Amount(50.01)));

        assertEquals(0, new BigDecimal("50.0").compareTo(ledger.getBalance(a)));
        assertEquals(1, ledger.getTransactions(a).size()); // only the deposit, no failed withdraw recorded
    }

    @Test
    void withdrawExactBalanceSucceeds() {
        AccountId a = new AccountId(1);
        ledger.deposit(a, new Amount(50.0));

        assertTrue(ledger.withdraw(a, new Amount(50.0)));

        assertEquals(0, BigDecimal.ZERO.compareTo(ledger.getBalance(a)));
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        AccountId a = new AccountId(1);
        AccountId b = new AccountId(2);
        ledger.deposit(a, new Amount(100.0));

        assertTrue(ledger.transfer(a, b, new Amount(40.0)));

        assertEquals(0, new BigDecimal("60.0").compareTo(ledger.getBalance(a)));
        assertEquals(0, new BigDecimal("40.0").compareTo(ledger.getBalance(b)));
    }

    @Test
    void transferFailsWhenInsufficientFundsAndNeitherBalanceChanges() {
        AccountId a = new AccountId(1);
        AccountId b = new AccountId(2);
        ledger.deposit(a, new Amount(10.0));

        assertFalse(ledger.transfer(a, b, new Amount(20.0)));

        assertEquals(0, new BigDecimal("10.0").compareTo(ledger.getBalance(a)));
        assertEquals(0, BigDecimal.ZERO.compareTo(ledger.getBalance(b)));
    }

    @Test
    void transferToSameAccountIsRejected() {
        AccountId a = new AccountId(1);
        ledger.deposit(a, new Amount(10.0));

        assertThrows(IllegalArgumentException.class, () -> ledger.transfer(a, a, new Amount(1.0)));
    }

    @Test
    void nullAccountOrAmountIsRejected() {
        AccountId a = new AccountId(1);

        assertThrows(IllegalArgumentException.class, () -> ledger.deposit(null, new Amount(1.0)));
        assertThrows(IllegalArgumentException.class, () -> ledger.deposit(a, null));
    }

    @Test
    void zeroOrNegativeAmountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Amount(0.0));
        assertThrows(IllegalArgumentException.class, () -> new Amount(-1.0));
    }

    @Test
    void nanAndInfiniteAmountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Amount(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Amount(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Amount(Double.NEGATIVE_INFINITY));
    }

    @Test
    void getBalanceForUntouchedAccountIsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(ledger.getBalance(new AccountId(999))));
    }

    @Test
    void timestampRangeQueryIsInclusiveOnBothBounds() {
        AccountId a = new AccountId(1);
        ledger.deposit(a, new Amount(1.0));
        ledger.deposit(a, new Amount(2.0));
        ledger.deposit(a, new Amount(3.0));

        List<Transaction> all = ledger.getTransactions(a);
        long from = all.get(0).getTimestamp();
        long to = all.get(all.size() - 1).getTimestamp();

        assertEquals(3, ledger.getTransactionsByTimeStampRange(from, to).size());
        assertEquals(0, ledger.getTransactionsByTimeStampRange(from - 1, from - 1).size());
    }

    @Test
    void concurrentWithdrawsAtBalanceBoundaryNeverOverdraw() throws InterruptedException {
        AccountId a = new AccountId(1);
        int attempts = 200;
        ledger.deposit(a, new Amount(100.0)); // exactly enough for 100 of the 200 withdrawals below

        // pool must be able to run all `attempts` tasks at once: they rendezvous on `ready`
        // before any of them proceeds, so a smaller pool would deadlock (queued tasks can
        // never reach the rendezvous while the running ones wait on it)
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            pool.submit(() -> {
                ready.countDown();
                await(start);
                if (ledger.withdraw(a, new Amount(1.0))) {
                    succeeded.incrementAndGet();
                }
            });
        }
        ready.await();
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(100, succeeded.get());
        assertEquals(0, BigDecimal.ZERO.compareTo(ledger.getBalance(a)));
        assertEquals(100 + 1, ledger.getTransactions(a).size()); // 1 deposit + 100 successful withdrawals
    }

    @Test
    void concurrentTransfersConserveTotalMoney() throws InterruptedException {
        AccountId a = new AccountId(1);
        AccountId b = new AccountId(2);
        ledger.deposit(a, new Amount(1000.0));
        ledger.deposit(b, new Amount(1000.0));

        int transfersPerDirection = 500;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch done = new CountDownLatch(transfersPerDirection * 2);

        for (int i = 0; i < transfersPerDirection; i++) {
            pool.submit(() -> {
                ledger.transfer(a, b, new Amount(1.0));
                done.countDown();
            });
            pool.submit(() -> {
                ledger.transfer(b, a, new Amount(1.0));
                done.countDown();
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdown();

        BigDecimal total = ledger.getBalance(a).add(ledger.getBalance(b));
        assertEquals(0, new BigDecimal("2000.0").compareTo(total));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
