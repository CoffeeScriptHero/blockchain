package com.kozarenko.lab1;

import java.util.List;

public class Block {

    private final int KDO_index;
    private final long KDO_timestamp;
    private final List<Transaction> KDO_transactions;
    private final int KDO_nonce;
    private final String KDO_prevHash;

    public Block(int KDO_index, int KDO_nonce, String KDO_prevHash, List<Transaction> KDO_transactions) {
        this.KDO_index = KDO_index;
        this.KDO_timestamp = System.currentTimeMillis();
        this.KDO_nonce = KDO_nonce;
        this.KDO_prevHash = KDO_prevHash;
        this.KDO_transactions = KDO_transactions;
    }

    public int getKDO_Index() {
        return KDO_index;
    }

    public long getKDO_Timestamp() {
        return KDO_timestamp;
    }

    public List<Transaction> getKDO_Transactions() {
        return KDO_transactions;
    }

    public int getKDO_Nonce() {
        return KDO_nonce;
    }

    public String getKDO_PrevHash() {
        return KDO_prevHash;
    }

    @Override
    public String toString() {
        return String.format("com.practice.Block{prevHash=%s, hash=%s}", KDO_prevHash, Blockchain.KDO_hash(this));
    }
}
