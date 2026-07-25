package com.ajay.transactionledger;

import com.ajay.transactionledger.impl.TransactionLedgerImpl;

public class Main {
    public static void main(String[] args) {
        TransactionLedger ledger = new TransactionLedgerImpl();

        AccountId a = new AccountId(1);
        AccountId b = new AccountId(2);

        ledger.deposit(a, new Amount(100.0));
        System.out.println("withdraw too much -> " + ledger.withdraw(a, new Amount(500.0)));
        System.out.println("transfer 40 a->b -> " + ledger.transfer(a, b, new Amount(40.0)));
        System.out.println("balance a=" + ledger.getBalance(a) + " b=" + ledger.getBalance(b));

        System.out.println("a's transactions:");
        for (Transaction t : ledger.getTransactions(a)) {
            System.out.println("  " + t);
        }
    }
}
