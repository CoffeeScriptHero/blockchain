package com.kozarenko.lab3;

import java.util.ArrayList;
import java.util.List;

public class MemoryPool {

    private final List<Transaction> transactions = new ArrayList<>();

    public List<Transaction> getTransactions() {
        return transactions.stream().toList();
    }

    public void add(Transaction transaction) {
        transactions.add(transaction);
    }

    public void clear() {
        transactions.clear();
    }
}
