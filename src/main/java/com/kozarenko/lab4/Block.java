package com.kozarenko.lab4;

import java.util.List;

public class Block {

    private final int index;
    private final long timestamp;
    private final List<Transaction> transactions;
    private final int nonce;
    private final String prevHash;

    public Block(int index, int nonce, String prevHash, List<Transaction> transactions) {
        this.index = index;
        this.timestamp = System.currentTimeMillis();
        this.nonce = nonce;
        this.prevHash = prevHash;
        this.transactions = transactions;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getNonce() {
        return nonce;
    }

    public String getPrevHash() {
        return prevHash;
    }

    @Override
    public String toString() {
        return String.format("Block{prevHash: %s, hash: %s}", prevHash, Blockchain.hash(this));
    }
}
