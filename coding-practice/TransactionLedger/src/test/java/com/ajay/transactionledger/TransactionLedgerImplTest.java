package com.ajay.transactionledger;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionLedgerImplTest {

    @Test
    void depositIncreasesBalance() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 100);
        assertEquals(100, ledger.getBalance(1));
    }

    @Test
    void withdrawDecreasesBalance() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 100);
        ledger.withdraw(1, 40);
        assertEquals(60, ledger.getBalance(1));
    }

    @Test
    void withdrawBeyondBalanceThrowsAndLeavesBalanceUnchanged() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 50);
        assertThrows(IllegalStateException.class, () -> ledger.withdraw(1, 100));
        assertEquals(50, ledger.getBalance(1));
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 100);
        ledger.transfer(1, 2, 30);
        assertEquals(70, ledger.getBalance(1));
        assertEquals(30, ledger.getBalance(2));
    }

    @Test
    void failedTransferDoesNotDebitSource() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 10);
        assertThrows(IllegalStateException.class, () -> ledger.transfer(1, 2, 100));
        assertEquals(10, ledger.getBalance(1));
        assertEquals(0, ledger.getBalance(2));
    }

    @Test
    void getTransactionsReturnsOnlyThatAccountsTransactionsInOrder() {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 100);
        ledger.deposit(2, 100);
        ledger.transfer(1, 2, 20);
        ledger.withdraw(2, 10);

        List<Transaction> account1Txns = ledger.getTransactions(1);
        assertEquals(2, account1Txns.size());
        assertEquals(TransactionType.DEPOSIT, account1Txns.get(0).type());
        assertEquals(TransactionType.TRANSFER, account1Txns.get(1).type());

        List<Transaction> account2Txns = ledger.getTransactions(2);
        assertEquals(3, account2Txns.size());
    }

    @Test
    void getTransactionsByTimestampRangeIsInclusiveAndSortedAcrossAccounts() throws InterruptedException {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 10);
        Thread.sleep(5);
        long from = System.currentTimeMillis();
        ledger.deposit(2, 20);
        ledger.transfer(1, 2, 5);
        long to = System.currentTimeMillis();
        Thread.sleep(5);
        ledger.deposit(1, 999);

        List<Transaction> inRange = ledger.getTransactionsByTimestampRange(from, to);

        assertEquals(2, inRange.size());
        for (int i = 1; i < inRange.size(); i++) {
            assertTrue(inRange.get(i - 1).timestamp() <= inRange.get(i).timestamp());
        }
    }

    @Test
    void concurrentDepositsAreNotLost() throws Exception {
        TransactionLedger ledger = new TransactionLedgerImpl();
        int threadCount = 50;
        int depositsPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                await(start);
                for (int j = 0; j < depositsPerThread; j++) {
                    ledger.deposit(1, 1);
                }
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals((long) threadCount * depositsPerThread, ledger.getBalance(1));
        assertEquals(threadCount * depositsPerThread, ledger.getTransactions(1).size());
    }

    @Test
    void concurrentTransfersConserveTotalBalance() throws Exception {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 10_000);
        int threadCount = 40;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            boolean forward = i % 2 == 0;
            futures.add(pool.submit(() -> {
                await(start);
                for (int j = 0; j < 50; j++) {
                    try {
                        if (forward) {
                            ledger.transfer(1, 2, 1);
                        } else {
                            ledger.transfer(2, 1, 1);
                        }
                    } catch (IllegalStateException ignoredInsufficientFunds) {
                        // expected when an account is transiently short of funds
                    }
                }
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(10_000, ledger.getBalance(1) + ledger.getBalance(2));
    }

    @Test
    void concurrentTransactionsKeepSortedListOrdered() throws Exception {
        TransactionLedger ledger = new TransactionLedgerImpl();
        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int accountId = i;
            futures.add(pool.submit(() -> {
                await(start);
                for (int j = 0; j < 25; j++) {
                    ledger.deposit(accountId, 1);
                }
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        List<Transaction> all = ledger.getTransactionsByTimestampRange(0, Long.MAX_VALUE);
        assertEquals(threadCount * 25, all.size());
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i - 1).timestamp() <= all.get(i).timestamp());
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
