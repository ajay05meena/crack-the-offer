package com.ajay.transactionledger;

public class Main {
    public static void main(String[] args) {
        TransactionLedger ledger = new TransactionLedgerImpl();
        ledger.deposit(1, 100);
        ledger.deposit(2, 50);
        ledger.transfer(1, 2, 30);
        System.out.println("Account 1 balance: " + ledger.getBalance(1));
        System.out.println("Account 2 balance: " + ledger.getBalance(2));
    }
}
